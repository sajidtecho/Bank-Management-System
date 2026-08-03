package com.bank.repository;

import com.bank.model.*;
import com.bank.utility.DateTimeUtil;
import com.bank.utility.FileUtil;

import java.time.LocalDateTime;
import java.util.*;

/**
 * File persistence implementation of {@link AccountRepository}.
 * Manages in-memory maps and serializes/deserializes records to text files in CSV format.
 */
public class AccountRepositoryImpl implements AccountRepository {

    private static final String ACCOUNTS_FILE = "src/main/resources/accounts.txt";
    private static final String TRANSACTIONS_FILE = "src/main/resources/transactions.txt";
    private static final String ADMINS_FILE = "src/main/resources/admins.txt";

    private final Map<String, Customer> customerMap = new HashMap<>(); // Key: AccountNumber
    private final Map<String, Admin> adminMap = new HashMap<>();       // Key: Username

    public AccountRepositoryImpl() {
        // Load data on instantiation
        loadData();
    }

    @Override
    public void loadData() {
        customerMap.clear();
        adminMap.clear();

        // 1. Ensure file directory infrastructure is present
        FileUtil.ensureFileExists(ADMINS_FILE);
        FileUtil.ensureFileExists(ACCOUNTS_FILE);
        FileUtil.ensureFileExists(TRANSACTIONS_FILE);

        // 2. Load Admins
        List<String> adminLines = FileUtil.readLines(ADMINS_FILE);
        if (adminLines.isEmpty()) {
            // Seed a default admin if none exists
            Admin defaultAdmin = new Admin("ADM1001", "System Administrator", "admin@bank.com", "9999999999", "Bank HQ", "admin", "admin123");
            saveAdmin(defaultAdmin);
            saveData();
        } else {
            for (String line : adminLines) {
                String[] parts = line.split("\\|");
                if (parts.length >= 7) {
                    Admin admin = new Admin(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
                    adminMap.put(admin.getUsername().toLowerCase(), admin);
                }
            }
        }

        // 3. Load Customers and Accounts
        List<String> accountLines = FileUtil.readLines(ACCOUNTS_FILE);
        for (String line : accountLines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 14) {
                String id = parts[0];
                String name = parts[1];
                String email = parts[2];
                String phone = parts[3];
                String address = parts[4];
                int age = Integer.parseInt(parts[5]);
                String gender = parts[6];
                String accNum = parts[7];
                String pin = parts[8];
                double balance = Double.parseDouble(parts[9]);
                AccountType type = AccountType.valueOf(parts[10]);
                LocalDateTime dateCreated = DateTimeUtil.parseDateTime(parts[11]);
                boolean locked = Boolean.parseBoolean(parts[12]);
                int failedAttempts = Integer.parseInt(parts[13]);

                Account account;
                if (type == AccountType.SAVINGS) {
                    account = new SavingsAccount(accNum, pin, balance);
                } else {
                    account = new CurrentAccount(accNum, pin, balance);
                }
                account.setDateCreated(dateCreated);
                account.setLocked(locked);
                account.setFailedAttempts(failedAttempts);

                Customer customer = new Customer(id, name, email, phone, address, age, gender, account);
                customerMap.put(accNum, customer);
            }
        }

        // 4. Load Transactions and associate them with customer accounts
        List<String> txnLines = FileUtil.readLines(TRANSACTIONS_FILE);
        for (String line : txnLines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                String txnId = parts[0];
                String accNum = parts[1];
                LocalDateTime dateTime = DateTimeUtil.parseDateTime(parts[2]);
                TransactionType type = TransactionType.valueOf(parts[3]);
                double amount = Double.parseDouble(parts[4]);
                double balanceAfter = Double.parseDouble(parts[5]);

                Transaction transaction = new Transaction(txnId, dateTime, type, amount, balanceAfter);
                Customer customer = customerMap.get(accNum);
                if (customer != null && customer.getAccount() != null) {
                    customer.getAccount().addTransaction(transaction);
                }
            }
        }
    }

    @Override
    public void saveData() {
        // 1. Serialize Admins
        List<String> adminLines = new ArrayList<>();
        for (Admin admin : adminMap.values()) {
            adminLines.add(String.join("|",
                    admin.getId(),
                    admin.getName(),
                    admin.getEmail(),
                    admin.getPhone(),
                    admin.getAddress(),
                    admin.getUsername(),
                    admin.getPassword()
            ));
        }
        FileUtil.writeLines(ADMINS_FILE, adminLines);

        // 2. Serialize Customers & Accounts
        List<String> accountLines = new ArrayList<>();
        List<String> txnLines = new ArrayList<>();

        for (Customer c : customerMap.values()) {
            Account acc = c.getAccount();
            if (acc != null) {
                accountLines.add(String.join("|",
                        c.getId(),
                        c.getName(),
                        c.getEmail(),
                        c.getPhone(),
                        c.getAddress(),
                        String.valueOf(c.getAge()),
                        c.getGender(),
                        acc.getAccountNumber(),
                        acc.getPin(),
                        String.valueOf(acc.getBalance()),
                        acc.getAccountType().name(),
                        DateTimeUtil.formatDateTime(acc.getDateCreated()),
                        String.valueOf(acc.isLocked()),
                        String.valueOf(acc.getFailedAttempts())
                ));

                // Grab and format transactions to lines
                for (Transaction txn : acc.getTransactions()) {
                    txnLines.add(String.join("|",
                            txn.getTransactionId(),
                            acc.getAccountNumber(),
                            DateTimeUtil.formatDateTime(txn.getDateTime()),
                            txn.getType().name(),
                            String.valueOf(txn.getAmount()),
                            String.valueOf(txn.getBalanceAfter())
                    ));
                }
            }
        }
        FileUtil.writeLines(ACCOUNTS_FILE, accountLines);
        FileUtil.writeLines(TRANSACTIONS_FILE, txnLines);
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
            customerMap.remove(accountNumber);
            saveData();
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
            saveData();
        }
    }
}
