package com.bank.repository;

import com.bank.model.*;
import com.bank.utility.DatabaseUtil;
import com.bank.utility.DateTimeUtil;
import com.bank.utility.FileUtil;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * PostgreSQL database implementation of {@link AccountRepository}.
 * Extends database storage using JDBC drivers while synchronizing with an in-memory map
 * to satisfy the existing service-layer transaction patterns.
 */
public class DatabaseAccountRepository implements AccountRepository {

    private final Map<String, Customer> customerMap = new HashMap<>(); // Key: AccountNumber
    private final Map<String, Admin> adminMap = new HashMap<>();       // Key: Username

    public DatabaseAccountRepository() {
        // Initialize schema and load data from database
        loadData();
    }

    @Override
    public void loadData() {
        customerMap.clear();
        adminMap.clear();

        // 1. Initialize schema tables
        initializeSchema();

        // 2. Load Admins
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name, email, phone, address, username, password FROM admins");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Admin admin = new Admin(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("username"),
                        rs.getString("password")
                );
                adminMap.put(admin.getUsername().toLowerCase(), admin);
            }
        } catch (SQLException e) {
            System.err.println("Error loading admins from database: " + e.getMessage());
        }

        // 3. Seed default admin if none exists
        if (adminMap.isEmpty()) {
            Admin defaultAdmin = new Admin("ADM1001", "System Administrator", "admin@bank.com", "9999999999", "Bank HQ", "admin", "admin123");
            saveAdmin(defaultAdmin);
        }

        // 4. Load Customers and Accounts
        String customerQuery = "SELECT c.id, c.name, c.email, c.phone, c.address, c.age, c.gender, " +
                "a.account_number, a.pin, a.balance, a.type, a.date_created, a.locked, a.failed_attempts " +
                "FROM customers c " +
                "JOIN accounts a ON c.id = a.customer_id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(customerQuery);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String accNum = rs.getString("account_number");
                String pin = rs.getString("pin");
                double balance = rs.getDouble("balance");
                AccountType type = AccountType.valueOf(rs.getString("type"));
                LocalDateTime dateCreated = rs.getTimestamp("date_created").toLocalDateTime();
                boolean locked = rs.getBoolean("locked");
                int failedAttempts = rs.getInt("failed_attempts");

                Account account;
                if (type == AccountType.SAVINGS) {
                    account = new SavingsAccount(accNum, pin, balance);
                } else {
                    account = new CurrentAccount(accNum, pin, balance);
                }
                account.setDateCreated(dateCreated);
                account.setLocked(locked);
                account.setFailedAttempts(failedAttempts);

                Customer customer = new Customer(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        account
                );
                customerMap.put(accNum, customer);
            }
        } catch (SQLException e) {
            System.err.println("Error loading customer accounts from database: " + e.getMessage());
        }

        // 5. Load Transactions and associate them with customer accounts
        String txnQuery = "SELECT transaction_id, account_number, date_time, type, amount, balance_after " +
                "FROM transactions " +
                "ORDER BY date_time ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(txnQuery);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String txnId = rs.getString("transaction_id");
                String accNum = rs.getString("account_number");
                LocalDateTime dateTime = rs.getTimestamp("date_time").toLocalDateTime();
                TransactionType type = TransactionType.valueOf(rs.getString("type"));
                double amount = rs.getDouble("amount");
                double balanceAfter = rs.getDouble("balance_after");

                Transaction transaction = new Transaction(txnId, dateTime, type, amount, balanceAfter);
                Customer customer = customerMap.get(accNum);
                if (customer != null && customer.getAccount() != null) {
                    customer.getAccount().addTransaction(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading transactions from database: " + e.getMessage());
        }
    }

    private void initializeSchema() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            // Read lines from schema.sql
            List<String> lines = FileUtil.readLines("src/main/resources/schema.sql");
            StringBuilder sqlBuilder = new StringBuilder();
            for (String line : lines) {
                if (!line.trim().startsWith("--")) {
                    sqlBuilder.append(line).append("\n");
                }
            }
            // Execute statements split by semicolon
            String[] statements = sqlBuilder.toString().split(";");
            for (String sql : statements) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to initialize database tables: " + e.getMessage());
        }
    }

    @Override
    public void saveData() {
        // Synchronize in-memory changes back to the database tables
        String insertCustomerSql = "INSERT INTO customers (id, name, email, phone, address, age, gender) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, email = EXCLUDED.email, phone = EXCLUDED.phone, " +
                "address = EXCLUDED.address, age = EXCLUDED.age, gender = EXCLUDED.gender";

        String insertAccountSql = "INSERT INTO accounts (account_number, customer_id, pin, balance, type, date_created, locked, failed_attempts) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (account_number) DO UPDATE SET " +
                "pin = EXCLUDED.pin, balance = EXCLUDED.balance, locked = EXCLUDED.locked, failed_attempts = EXCLUDED.failed_attempts";

        String insertTxnSql = "INSERT INTO transactions (transaction_id, account_number, date_time, type, amount, balance_after) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (transaction_id) DO NOTHING";

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false); // Make save batch atomic

            try (PreparedStatement psCust = conn.prepareStatement(insertCustomerSql);
                 PreparedStatement psAcc = conn.prepareStatement(insertAccountSql);
                 PreparedStatement psTxn = conn.prepareStatement(insertTxnSql)) {

                for (Customer c : customerMap.values()) {
                    // Update Customer info
                    psCust.setString(1, c.getId());
                    psCust.setString(2, c.getName());
                    psCust.setString(3, c.getEmail());
                    psCust.setString(4, c.getPhone());
                    psCust.setString(5, c.getAddress());
                    psCust.setInt(6, c.getAge());
                    psCust.setString(7, c.getGender());
                    psCust.addBatch();

                    // Update Account info
                    Account acc = c.getAccount();
                    if (acc != null) {
                        psAcc.setString(1, acc.getAccountNumber());
                        psAcc.setString(2, c.getId());
                        psAcc.setString(3, acc.getPin());
                        psAcc.setDouble(4, acc.getBalance());
                        psAcc.setString(5, acc.getAccountType().name());
                        psAcc.setTimestamp(6, Timestamp.valueOf(acc.getDateCreated()));
                        psAcc.setBoolean(7, acc.isLocked());
                        psAcc.setInt(8, acc.getFailedAttempts());
                        psAcc.addBatch();

                        // Update Transactions info
                        for (Transaction txn : acc.getTransactions()) {
                            psTxn.setString(1, txn.getTransactionId());
                            psTxn.setString(2, acc.getAccountNumber());
                            psTxn.setTimestamp(3, Timestamp.valueOf(txn.getDateTime()));
                            psTxn.setString(4, txn.getType().name());
                            psTxn.setDouble(5, txn.getAmount());
                            psTxn.setDouble(6, txn.getBalanceAfter());
                            psTxn.addBatch();
                        }
                    }
                }

                psCust.executeBatch();
                psAcc.executeBatch();
                psTxn.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error synchronizing repository data to database: " + e.getMessage());
        }
    }

    @Override
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customerMap.values());
    }

    @Override
    public Optional<Customer> getCustomerByAccountNumber(String accountNumber) {
        return Optional.ofNullable(customerMap.get(accountNumber));
    }

    @Override
    public Optional<Customer> getCustomerByPhone(String phone) {
        return customerMap.values().stream()
                .filter(c -> c.getPhone().equals(phone))
                .findFirst();
    }

    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerMap.values().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public void saveCustomer(Customer customer) {
        if (customer != null && customer.getAccount() != null) {
            customerMap.put(customer.getAccount().getAccountNumber(), customer);
            saveData();
        }
    }

    @Override
    public boolean deleteCustomer(String accountNumber) {
        if (customerMap.containsKey(accountNumber)) {
            Customer customer = customerMap.remove(accountNumber);
            saveData(); // Sync maps with database

            // Explicitly delete from database to handle CASCADE rules
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM customers WHERE id = ?")) {
                ps.setString(1, customer.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error deleting customer database record: " + e.getMessage());
            }
            return true;
        }
        return false;
    }

    @Override
    public List<Admin> getAllAdmins() {
        return new ArrayList<>(adminMap.values());
    }

    @Override
    public Optional<Admin> getAdminByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(adminMap.get(username.toLowerCase()));
    }

    @Override
    public void saveAdmin(Admin admin) {
        if (admin != null) {
            adminMap.put(admin.getUsername().toLowerCase(), admin);

            String insertAdminSql = "INSERT INTO admins (id, name, email, phone, address, username, password) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (id) DO UPDATE SET " +
                    "name = EXCLUDED.name, email = EXCLUDED.email, phone = EXCLUDED.phone, " +
                    "address = EXCLUDED.address, username = EXCLUDED.username, password = EXCLUDED.password";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insertAdminSql)) {
                ps.setString(1, admin.getId());
                ps.setString(2, admin.getName());
                ps.setString(3, admin.getEmail());
                ps.setString(4, admin.getPhone());
                ps.setString(5, admin.getAddress());
                ps.setString(6, admin.getUsername());
                ps.setString(7, admin.getPassword());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error saving admin to database: " + e.getMessage());
            }
        }
    }
}
