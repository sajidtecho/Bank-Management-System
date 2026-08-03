package com.bank.service;

import com.bank.exception.*;
import com.bank.model.*;
import com.bank.repository.AccountRepository;
import com.bank.utility.AccountNumberGenerator;
import com.bank.utility.ReceiptGenerator;
import com.bank.utility.ValidationUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation containing core business policies and database synchronizations.
 */
public class BankServiceImpl implements BankService {

    private final AccountRepository repository;

    public BankServiceImpl(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer createAccount(String name, int age, String gender, String phone, String email, 
                                   String address, AccountType type, String pin, double openingBalance) 
            throws DuplicateAccountException, InvalidAmountException {

        // 1. Input validations
        if (!ValidationUtil.isValidName(name)) {
            throw new IllegalArgumentException("Invalid customer name. Only alphabetical characters and spaces are allowed.");
        }
        if (!ValidationUtil.isValidAge(age)) {
            throw new IllegalArgumentException("Invalid age. Customers must be between 18 and 100 years old.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone number. Must contain exactly 10 digits.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!ValidationUtil.isValidPin(pin)) {
            throw new IllegalArgumentException("Invalid PIN. Must contain exactly 4 digits.");
        }

        // 2. Duplicate checks
        if (repository.getCustomerByPhone(phone).isPresent()) {
            throw new DuplicateAccountException("An account is already registered with phone number: " + phone);
        }
        if (repository.getCustomerByEmail(email).isPresent()) {
            throw new DuplicateAccountException("An account is already registered with email: " + email);
        }

        // 3. Minimum balance validations
        Account account;
        String accNum = generateUniqueAccountNumber();

        if (type == AccountType.SAVINGS) {
            account = new SavingsAccount(accNum, pin, openingBalance);
        } else {
            account = new CurrentAccount(accNum, pin, openingBalance);
        }

        if (openingBalance < account.getMinBalance()) {
            throw new InvalidAmountException("Opening balance cannot be less than the minimum required balance of INR " + account.getMinBalance());
        }

        // 4. Create customer entity
        String customerId = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Customer customer = new Customer(customerId, name, email, phone, address, age, gender, account);

        // 5. Save customer and trigger auto-save
        repository.saveCustomer(customer);
        return customer;
    }

    @Override
    public User login(String accountOrUsername, String pinOrPassword, boolean isAdmin) 
            throws AccountNotFoundException, InvalidPinException, AccountLockedException {

        if (isAdmin) {
            Admin admin = repository.getAdminByUsername(accountOrUsername)
                    .orElseThrow(() -> new AccountNotFoundException("Admin username not found: " + accountOrUsername));
            if (!admin.getPassword().equals(pinOrPassword)) {
                throw new InvalidPinException("Incorrect Admin Password.");
            }
            return admin;
        } else {
            Customer customer = repository.getCustomerByAccountNumber(accountOrUsername)
                    .orElseThrow(() -> new AccountNotFoundException("Account number not found: " + accountOrUsername));

            Account account = customer.getAccount();
            if (account.isLocked()) {
                throw new AccountLockedException("Your account is locked due to 3 consecutive incorrect login attempts. Please contact the administrator.");
            }

            if (!account.getPin().equals(pinOrPassword)) {
                account.setFailedAttempts(account.getFailedAttempts() + 1);
                if (account.getFailedAttempts() >= 3) {
                    account.setLocked(true);
                    repository.saveData(); // Save lock status
                    throw new AccountLockedException("Incorrect PIN. Your account has now been locked. Please contact the administrator.");
                }
                repository.saveData(); // Sync failed attempt count
                throw new InvalidPinException("Incorrect PIN. Attempts remaining: " + (3 - account.getFailedAttempts()));
            }

            // Success: Reset failed attempts
            account.setFailedAttempts(0);
            repository.saveData();
            return customer;
        }
    }

    @Override
    public void deposit(String accountNumber, double amount) 
            throws AccountNotFoundException, InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive.");
        }

        Customer customer = repository.getCustomerByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account number not found: " + accountNumber));

        Account account = customer.getAccount();
        double prevBalance = account.getBalance();
        double currentBalance = prevBalance + amount;
        account.setBalance(currentBalance);

        // Create transaction log
        String txnId = "TXN" + System.nanoTime();
        Transaction txn = new Transaction(txnId, TransactionType.DEPOSIT, amount, currentBalance);
        account.addTransaction(txn);

        // Sync files and generate receipt
        repository.saveData();
        ReceiptGenerator.generateReceipt(txnId, customer.getName(), accountNumber, "DEPOSIT", amount, prevBalance, currentBalance, "SUCCESS");
    }

    @Override
    public void withdraw(String accountNumber, String pin, double amount) 
            throws AccountNotFoundException, InvalidPinException, InsufficientBalanceException, 
                   InvalidAmountException, AccountLockedException {

        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive.");
        }

        Customer customer = repository.getCustomerByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account number not found: " + accountNumber));

        Account account = customer.getAccount();
        if (account.isLocked()) {
            throw new AccountLockedException("Account is locked. Withdrawals are disabled.");
        }

        if (!account.getPin().equals(pin)) {
            throw new InvalidPinException("Incorrect PIN.");
        }

        double prevBalance = account.getBalance();
        double minBalance = account.getMinBalance();

        if (prevBalance - amount < minBalance) {
            throw new InsufficientBalanceException(String.format(
                    "Insufficient balance. Your current balance is INR %,.2f. Min balance required is INR %,.2f. Limit exceeded by INR %,.2f.",
                    prevBalance, minBalance, (minBalance - (prevBalance - amount))));
        }

        double currentBalance = prevBalance - amount;
        account.setBalance(currentBalance);

        // Log transaction
        String txnId = "TXN" + System.nanoTime();
        Transaction txn = new Transaction(txnId, TransactionType.WITHDRAWAL, amount, currentBalance);
        account.addTransaction(txn);

        repository.saveData();
        ReceiptGenerator.generateReceipt(txnId, customer.getName(), accountNumber, "WITHDRAWAL", amount, prevBalance, currentBalance, "SUCCESS");
    }

    @Override
    public void transfer(String senderAccountNumber, String pin, String receiverAccountNumber, double amount) 
            throws AccountNotFoundException, InvalidPinException, InsufficientBalanceException, 
                   InvalidAmountException, AccountLockedException {

        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive.");
        }

        if (senderAccountNumber.equals(receiverAccountNumber)) {
            throw new IllegalArgumentException("Sender and Receiver accounts cannot be the same.");
        }

        Customer sender = repository.getCustomerByAccountNumber(senderAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Sender account number not found."));

        Customer receiver = repository.getCustomerByAccountNumber(receiverAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account number not found."));

        Account senderAcc = sender.getAccount();
        Account receiverAcc = receiver.getAccount();

        if (senderAcc.isLocked()) {
            throw new AccountLockedException("Sender account is locked. Transfers are disabled.");
        }
        if (receiverAcc.isLocked()) {
            throw new AccountLockedException("Receiver account is currently locked/inactive.");
        }

        if (!senderAcc.getPin().equals(pin)) {
            throw new InvalidPinException("Incorrect Sender PIN.");
        }

        double senderPrevBalance = senderAcc.getBalance();
        double senderMinBalance = senderAcc.getMinBalance();

        if (senderPrevBalance - amount < senderMinBalance) {
            throw new InsufficientBalanceException(String.format(
                    "Insufficient funds. Transfer failed. Balance is INR %,.2f. Minimum balance is INR %,.2f.",
                    senderPrevBalance, senderMinBalance));
        }

        // Perform Transfer atomically
        double senderCurrentBalance = senderPrevBalance - amount;
        senderAcc.setBalance(senderCurrentBalance);

        double receiverPrevBalance = receiverAcc.getBalance();
        double receiverCurrentBalance = receiverPrevBalance + amount;
        receiverAcc.setBalance(receiverCurrentBalance);

        // Transaction log IDs
        String senderTxnId = "TXN" + System.nanoTime();
        String receiverTxnId = "TXN" + (System.nanoTime() + 1); // Make it unique

        // Log sender
        Transaction senderTxn = new Transaction(senderTxnId, TransactionType.TRANSFER_SENT, amount, senderCurrentBalance);
        senderAcc.addTransaction(senderTxn);

        // Log receiver
        Transaction receiverTxn = new Transaction(receiverTxnId, TransactionType.TRANSFER_RECEIVED, amount, receiverCurrentBalance);
        receiverAcc.addTransaction(receiverTxn);

        repository.saveData();

        // Generate receipt for sender
        ReceiptGenerator.generateReceipt(senderTxnId, sender.getName(), senderAccountNumber, "TRANSFER SENT TO " + receiverAccountNumber, amount, senderPrevBalance, senderCurrentBalance, "SUCCESS");
        // Generate receipt for receiver
        ReceiptGenerator.generateReceipt(receiverTxnId, receiver.getName(), receiverAccountNumber, "TRANSFER RECEIVED FROM " + senderAccountNumber, amount, receiverPrevBalance, receiverCurrentBalance, "SUCCESS");
    }

    @Override
    public Customer getProfile(String accountNumber) throws AccountNotFoundException {
        return repository.getCustomerByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Customer account not found: " + accountNumber));
    }

    @Override
    public void updateProfile(String accountNumber, String phone, String email, String address) 
            throws AccountNotFoundException {

        Customer customer = repository.getCustomerByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Customer account not found: " + accountNumber));

        if (phone != null && !phone.trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(phone)) {
                throw new IllegalArgumentException("Phone number must contain exactly 10 digits.");
            }
            customer.setPhone(phone);
        }

        if (email != null && !email.trim().isEmpty()) {
            if (!ValidationUtil.isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format.");
            }
            customer.setEmail(email);
        }

        if (address != null && !address.trim().isEmpty()) {
            customer.setAddress(address);
        }

        repository.saveData();
    }

    @Override
    public void changePin(String accountNumber, String oldPin, String newPin) 
            throws AccountNotFoundException, InvalidPinException {

        Customer customer = repository.getCustomerByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Customer account not found: " + accountNumber));

        Account account = customer.getAccount();
        if (!account.getPin().equals(oldPin)) {
            throw new InvalidPinException("Incorrect old PIN.");
        }

        if (!ValidationUtil.isValidPin(newPin)) {
            throw new InvalidPinException("New PIN must contain exactly 4 digits.");
        }

        account.setPin(newPin);
        repository.saveData();
    }

    @Override
    public boolean deleteAccount(String accountNumber) throws AccountNotFoundException {
        if (!repository.getCustomerByAccountNumber(accountNumber).isPresent()) {
            throw new AccountNotFoundException("Account number not found: " + accountNumber);
        }
        return repository.deleteCustomer(accountNumber);
    }

    @Override
    public List<Customer> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return repository.getAllCustomers();
        }

        String searchKey = query.toLowerCase().trim();
        return repository.getAllCustomers().stream()
                .filter(c -> c.getAccount().getAccountNumber().contains(searchKey) ||
                             c.getName().toLowerCase().contains(searchKey) ||
                             c.getPhone().contains(searchKey))
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> getAllCustomersSorted(String sortBy) {
        List<Customer> list = repository.getAllCustomers();

        if ("name".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER));
        } else if ("balance".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparingDouble((Customer c) -> c.getAccount().getBalance()).reversed());
        } else if ("accountnumber".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing((Customer c) -> c.getAccount().getAccountNumber()));
        }

        return list;
    }

    @Override
    public double getTotalDeposits() {
        return repository.getAllCustomers().stream()
                .mapToDouble(c -> c.getAccount().getBalance())
                .sum();
    }

    @Override
    public int getTotalAccountsCount() {
        return repository.getAllCustomers().size();
    }

    @Override
    public boolean unlockAccount(String accountNumber) throws AccountNotFoundException {
        Customer customer = repository.getCustomerByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        Account account = customer.getAccount();
        if (account.isLocked()) {
            account.setLocked(false);
            account.setFailedAttempts(0);
            repository.saveData();
            return true;
        }
        return false;
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountNumber) throws AccountNotFoundException {
        Customer customer = repository.getCustomerByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        return customer.getAccount().getTransactions();
    }

    private String generateUniqueAccountNumber() {
        Set<String> taken = repository.getAllCustomers().stream()
                .map(c -> c.getAccount().getAccountNumber())
                .collect(Collectors.toSet());
        return AccountNumberGenerator.generate(taken);
    }
}
