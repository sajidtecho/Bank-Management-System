package com.bank.menu;

import com.bank.controller.BankController;
import com.bank.exception.*;
import com.bank.model.*;
import com.bank.utility.ConsoleTable;
import com.bank.utility.DateTimeUtil;

import java.util.List;
import java.util.Scanner;

/**
 * Presentation Layer. Displays formatted text dashboards and accepts console user selections.
 */
public class ConsoleMenu {

    private final BankController controller;
    private final Scanner scanner;

    public ConsoleMenu(BankController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            printHeader("WELCOME TO BANK MANAGEMENT SYSTEM");
            System.out.println("1. Login");
            System.out.println("2. Create Account");
            System.out.println("3. Exit");
            System.out.println("==========================================");
            System.out.print("Select an option (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleCreateAccount();
                    break;
                case "3":
                    System.out.println("\nSaving data and shutting down. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    printError("Invalid choice! Please select 1, 2, or 3.");
            }
        }
    }

    private void handleLogin() {
        printHeader("LOGIN GATEWAY");
        System.out.println("1. Customer Login");
        System.out.println("2. Admin Login");
        System.out.println("3. Back");
        System.out.println("==========================================");
        System.out.print("Select an option (1-3): ");

        String choice = scanner.nextLine().trim();
        if (choice.equals("3")) return;

        boolean isAdmin = choice.equals("2");
        if (!choice.equals("1") && !choice.equals("2")) {
            printError("Invalid option.");
            return;
        }

        System.out.print(isAdmin ? "Enter Admin Username: " : "Enter 10-Digit Account Number: ");
        String idOrUser = scanner.nextLine().trim();
        System.out.print(isAdmin ? "Enter Admin Password: " : "Enter 4-Digit Security PIN: ");
        String pinOrPass = scanner.nextLine().trim();

        try {
            User user = controller.login(idOrUser, pinOrPass, isAdmin);
            printSuccess("Login successful! Welcome, " + user.getName() + ".");

            if (isAdmin) {
                showAdminDashboard((Admin) user);
            } else {
                showCustomerDashboard((Customer) user);
            }
        } catch (AccountNotFoundException | InvalidPinException | AccountLockedException e) {
            printError(e.getMessage());
        }
    }

    private void handleCreateAccount() {
        printHeader("CREATE NEW ACCOUNT");

        System.out.print("Enter Full Name (Alphabetical only): ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Age (18 - 100): ");
        int age;
        try {
            age = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            printError("Age must be a valid integer.");
            return;
        }

        System.out.print("Enter Gender (Male/Female/Other): ");
        String gender = scanner.nextLine().trim();

        System.out.print("Enter Phone Number (Exactly 10 digits): ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Residential Address: ");
        String address = scanner.nextLine().trim();

        System.out.println("Account Types:");
        System.out.println("1. SAVINGS (Min Balance Required: INR 500)");
        System.out.println("2. CURRENT (Min Balance Required: INR 2000)");
        System.out.print("Select Account Type (1-2): ");
        String typeChoice = scanner.nextLine().trim();

        AccountType type;
        if (typeChoice.equals("1")) {
            type = AccountType.SAVINGS;
        } else if (typeChoice.equals("2")) {
            type = AccountType.CURRENT;
        } else {
            printError("Invalid account type selection.");
            return;
        }

        System.out.print("Choose 4-Digit PIN: ");
        String pin = scanner.nextLine().trim();

        System.out.print("Enter Opening Deposit Balance (INR): ");
        double balance;
        try {
            balance = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            printError("Invalid balance format.");
            return;
        }

        try {
            Customer c = controller.createAccount(name, age, gender, phone, email, address, type, pin, balance);
            printSuccess("Account created successfully!");
            System.out.println("\nGenerated Account Number: " + c.getAccount().getAccountNumber());
            System.out.println("Keep your security PIN safe!");
        } catch (DuplicateAccountException | InvalidAmountException | IllegalArgumentException e) {
            printError(e.getMessage());
        }
    }

    private void showAdminDashboard(Admin admin) {
        while (true) {
            printHeader("ADMIN DASHBOARD (User: " + admin.getUsername() + ")");
            System.out.println("1. Create Customer Account");
            System.out.println("2. Delete Account");
            System.out.println("3. Search Customer");
            System.out.println("4. View All Customers");
            System.out.println("5. View Total Deposits");
            System.out.println("6. View Total Accounts");
            System.out.println("7. Unlock Locked Account");
            System.out.println("8. View Customer Transaction Logs");
            System.out.println("9. Logout");
            System.out.println("==========================================");
            System.out.print("Select an option (1-9): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleCreateAccount();
                    break;
                case "2":
                    handleDeleteAccount();
                    break;
                case "3":
                    handleSearchCustomer();
                    break;
                case "4":
                    handleViewAllCustomers();
                    break;
                case "5":
                    System.out.printf("\nTotal Pool Deposits: INR %,.2f\n", controller.getTotalDeposits());
                    break;
                case "6":
                    System.out.println("\nTotal Registered Accounts: " + controller.getTotalAccountsCount());
                    break;
                case "7":
                    handleUnlockAccount();
                    break;
                case "8":
                    handleViewTransactionLogs();
                    break;
                case "9":
                    printSuccess("Logged out of Admin Portal.");
                    return;
                default:
                    printError("Invalid choice.");
            }
        }
    }

    private void showCustomerDashboard(Customer customer) {
        String accNum = customer.getAccount().getAccountNumber();
        while (true) {
            // Refresh customer references
            try {
                customer = controller.getProfile(accNum);
            } catch (AccountNotFoundException e) {
                printError("Active profile reference lost. Logging out.");
                return;
            }

            printHeader("CUSTOMER DASHBOARD (A/C: " + accNum + ")");
            System.out.println("1. View Profile");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. Update Profile");
            System.out.println("6. Change PIN");
            System.out.println("7. View Mini Statement");
            System.out.println("8. View Transaction History");
            System.out.println("9. Logout");
            System.out.println("==========================================");
            System.out.print("Select an option (1-9): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleViewProfile(customer);
                    break;
                case "2":
                    handleDeposit(customer);
                    break;
                case "3":
                    handleWithdraw(customer);
                    break;
                case "4":
                    handleTransfer(customer);
                    break;
                case "5":
                    handleUpdateProfile(customer);
                    break;
                case "6":
                    handleChangePin(customer);
                    break;
                case "7":
                    handleMiniStatement(customer);
                    break;
                case "8":
                    handleFullHistory(customer);
                    break;
                case "9":
                    printSuccess("Logged out of Customer Portal.");
                    return;
                default:
                    printError("Invalid option.");
            }
        }
    }

    private void handleDeleteAccount() {
        System.out.print("Enter Account Number to delete: ");
        String accNum = scanner.nextLine().trim();

        System.out.print("Are you absolutely sure you want to delete this account? (YES/NO): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("YES")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        try {
            controller.deleteAccount(accNum);
            printSuccess("Account " + accNum + " has been permanently deleted.");
        } catch (AccountNotFoundException e) {
            printError(e.getMessage());
        }
    }

    private void handleSearchCustomer() {
        System.out.print("Enter Search Term (Account No / Name / Phone): ");
        String term = scanner.nextLine().trim();

        List<Customer> list = controller.searchCustomers(term);
        renderCustomersTable(list);
    }

    private void handleViewAllCustomers() {
        System.out.println("Sorting Options:");
        System.out.println("1. Sort by Customer Name");
        System.out.println("2. Sort by Available Balance");
        System.out.println("3. Sort by Account Number");
        System.out.print("Select sort preference (1-3): ");
        String sortChoice = scanner.nextLine().trim();

        String sortBy = "name";
        if (sortChoice.equals("2")) sortBy = "balance";
        else if (sortChoice.equals("3")) sortBy = "accountnumber";

        List<Customer> list = controller.getAllCustomersSorted(sortBy);
        renderCustomersTable(list);
    }

    private void handleUnlockAccount() {
        System.out.print("Enter Locked Account Number: ");
        String accNum = scanner.nextLine().trim();
        try {
            boolean unlocked = controller.unlockAccount(accNum);
            if (unlocked) {
                printSuccess("Account " + accNum + " has been successfully unlocked.");
            } else {
                printError("Account " + accNum + " was not locked.");
            }
        } catch (AccountNotFoundException e) {
            printError(e.getMessage());
        }
    }

    private void handleViewTransactionLogs() {
        System.out.print("Enter Customer Account Number: ");
        String accNum = scanner.nextLine().trim();
        try {
            List<Transaction> list = controller.getTransactionHistory(accNum);
            renderTransactionsTable(list);
        } catch (AccountNotFoundException e) {
            printError(e.getMessage());
        }
    }

    private void handleViewProfile(Customer customer) {
        printHeader("CUSTOMER PROFILE");
        System.out.printf("Customer ID  : %s\n", customer.getId());
        System.out.printf("Full Name    : %s\n", customer.getName());
        System.out.printf("Age          : %d\n", customer.getAge());
        System.out.printf("Gender       : %s\n", customer.getGender());
        System.out.printf("Phone Number : %s\n", customer.getPhone());
        System.out.printf("Email ID     : %s\n", customer.getEmail());
        System.out.printf("Address      : %s\n", customer.getAddress());
        System.out.printf("Account No   : %s\n", customer.getAccount().getAccountNumber());
        System.out.printf("Account Type : %s\n", customer.getAccount().getAccountType());
        System.out.printf("Balance      : INR %,.2f\n", customer.getAccount().getBalance());
        System.out.printf("Min Balance  : INR %,.2f\n", customer.getAccount().getMinBalance());
        System.out.printf("Date Created : %s\n", DateTimeUtil.formatDateTime(customer.getAccount().getDateCreated()));
        System.out.printf("Status       : %s\n", customer.getAccount().isLocked() ? "LOCKED" : "ACTIVE");
        System.out.println("==========================================");
    }

    private void handleDeposit(Customer customer) {
        System.out.print("Enter Amount to Deposit (INR): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            printError("Invalid amount format.");
            return;
        }

        try {
            controller.deposit(customer.getAccount().getAccountNumber(), amount);
            printSuccess("Deposit successful! Receipt generated inside receipts/ directory.");
        } catch (AccountNotFoundException | InvalidAmountException e) {
            printError(e.getMessage());
        }
    }

    private void handleWithdraw(Customer customer) {
        System.out.print("Enter Amount to Withdraw (INR): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            printError("Invalid amount format.");
            return;
        }

        System.out.print("Confirm 4-Digit Security PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            controller.withdraw(customer.getAccount().getAccountNumber(), pin, amount);
            printSuccess("Withdrawal successful! Receipt generated inside receipts/ directory.");
        } catch (AccountNotFoundException | InvalidPinException | InsufficientBalanceException | 
                 InvalidAmountException | AccountLockedException e) {
            printError(e.getMessage());
        }
    }

    private void handleTransfer(Customer customer) {
        System.out.print("Enter Destination 10-Digit Account Number: ");
        String receiverAcc = scanner.nextLine().trim();

        System.out.print("Enter Transfer Amount (INR): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            printError("Invalid amount format.");
            return;
        }

        System.out.print("Confirm Your 4-Digit Security PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            controller.transfer(customer.getAccount().getAccountNumber(), pin, receiverAcc, amount);
            printSuccess("Transfer successful! Receipts generated inside receipts/ directory.");
        } catch (AccountNotFoundException | InvalidPinException | InsufficientBalanceException | 
                 InvalidAmountException | AccountLockedException e) {
            printError(e.getMessage());
        }
    }

    private void handleUpdateProfile(Customer customer) {
        printHeader("UPDATE PROFILE DETAILS (Press Enter to skip fields)");
        System.out.print("Enter New Phone Number (10 digits) [" + customer.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter New Email Address [" + customer.getEmail() + "]: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter New Residential Address [" + customer.getAddress() + "]: ");
        String address = scanner.nextLine().trim();

        try {
            controller.updateProfile(customer.getAccount().getAccountNumber(), phone, email, address);
            printSuccess("Profile updated successfully!");
        } catch (AccountNotFoundException | IllegalArgumentException e) {
            printError(e.getMessage());
        }
    }

    private void handleChangePin(Customer customer) {
        printHeader("CHANGE SECURITY PIN");
        System.out.print("Enter Current PIN: ");
        String oldPin = scanner.nextLine().trim();
        System.out.print("Enter New 4-Digit PIN: ");
        String newPin = scanner.nextLine().trim();

        try {
            controller.changePin(customer.getAccount().getAccountNumber(), oldPin, newPin);
            printSuccess("PIN changed successfully!");
        } catch (AccountNotFoundException | InvalidPinException e) {
            printError(e.getMessage());
        }
    }

    private void handleMiniStatement(Customer customer) {
        List<Transaction> txns = customer.getAccount().getTransactions();
        if (txns.isEmpty()) {
            System.out.println("\nNo transaction history found.");
            return;
        }
        // Grab last 5 transactions
        List<Transaction> mini = txns.stream()
                .skip(Math.max(0, txns.size() - 5))
                .toList();

        printHeader("MINI STATEMENT (Last 5 Transactions)");
        renderTransactionsTable(mini);
    }

    private void handleFullHistory(Customer customer) {
        List<Transaction> txns = customer.getAccount().getTransactions();
        if (txns.isEmpty()) {
            System.out.println("\nNo transaction history found.");
            return;
        }
        printHeader("TRANSACTION HISTORY LOGS");
        renderTransactionsTable(txns);
    }

    // Helper table renderers
    private void renderCustomersTable(List<Customer> customers) {
        if (customers.isEmpty()) {
            System.out.println("\nNo customer accounts match criteria.");
            return;
        }
        ConsoleTable table = new ConsoleTable();
        table.setHeaders("A/C Number", "Full Name", "Age", "Phone", "Email ID", "Type", "Balance (INR)", "Status");
        for (Customer c : customers) {
            Account acc = c.getAccount();
            table.addRow(
                    acc.getAccountNumber(),
                    c.getName(),
                    String.valueOf(c.getAge()),
                    c.getPhone(),
                    c.getEmail(),
                    acc.getAccountType().name(),
                    String.format("%,.2f", acc.getBalance()),
                    acc.isLocked() ? "LOCKED" : "ACTIVE"
            );
        }
        table.print();
    }

    private void renderTransactionsTable(List<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println("\nNo records found.");
            return;
        }
        ConsoleTable table = new ConsoleTable();
        table.setHeaders("Transaction ID", "Date & Time", "Type", "Amount (INR)", "Running Balance");
        for (Transaction t : list) {
            table.addRow(
                    t.getTransactionId(),
                    DateTimeUtil.formatDateTime(t.getDateTime()),
                    t.getType().name(),
                    String.format("%,.2f", t.getAmount()),
                    String.format("%,.2f", t.getBalanceAfter())
            );
        }
        table.print();
    }

    // UI helpers
    private void printHeader(String title) {
        System.out.println("\n=======================================================");
        System.out.printf("       %s\n", title);
        System.out.println("=======================================================");
    }

    private void printSuccess(String msg) {
        System.out.println("\n[SUCCESS] " + msg);
    }

    private void printError(String msg) {
        System.out.println("\n[ERROR] " + msg);
    }
}
