package com.bank.controller;

import com.bank.exception.*;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.model.AccountType;
import com.bank.service.BankService;

import java.util.List;

/**
 * Controller class coordinating interactions between the Presentation Layer (ConsoleMenu)
 * and the Business Layer (BankService).
 */
public class BankController {

    private final BankService service;

    public BankController(BankService service) {
        this.service = service;
    }

    public Customer createAccount(String name, int age, String gender, String phone, String email, 
                                   String address, AccountType type, String pin, double openingBalance) 
            throws DuplicateAccountException, InvalidAmountException {
        return service.createAccount(name, age, gender, phone, email, address, type, pin, openingBalance);
    }

    public User login(String accountOrUsername, String pinOrPassword, boolean isAdmin) 
            throws AccountNotFoundException, InvalidPinException, AccountLockedException {
        return service.login(accountOrUsername, pinOrPassword, isAdmin);
    }

    public void deposit(String accountNumber, double amount) 
            throws AccountNotFoundException, InvalidAmountException {
        service.deposit(accountNumber, amount);
    }

    public void withdraw(String accountNumber, String pin, double amount) 
            throws AccountNotFoundException, InvalidPinException, InsufficientBalanceException, 
                   InvalidAmountException, AccountLockedException {
        service.withdraw(accountNumber, pin, amount);
    }

    public void transfer(String senderAccountNumber, String pin, String receiverAccountNumber, double amount) 
            throws AccountNotFoundException, InvalidPinException, InsufficientBalanceException, 
                   InvalidAmountException, AccountLockedException {
        service.transfer(senderAccountNumber, pin, receiverAccountNumber, amount);
    }

    public Customer getProfile(String accountNumber) throws AccountNotFoundException {
        return service.getProfile(accountNumber);
    }

    public void updateProfile(String accountNumber, String phone, String email, String address) 
            throws AccountNotFoundException {
        service.updateProfile(accountNumber, phone, email, address);
    }

    public void changePin(String accountNumber, String oldPin, String newPin) 
            throws AccountNotFoundException, InvalidPinException {
        service.changePin(accountNumber, oldPin, newPin);
    }

    public boolean deleteAccount(String accountNumber) throws AccountNotFoundException {
        return service.deleteAccount(accountNumber);
    }

    public List<Customer> searchCustomers(String query) {
        return service.searchCustomers(query);
    }

    public List<Customer> getAllCustomersSorted(String sortBy) {
        return service.getAllCustomersSorted(sortBy);
    }

    public double getTotalDeposits() {
        return service.getTotalDeposits();
    }

    public int getTotalAccountsCount() {
        return service.getTotalAccountsCount();
    }

    public boolean unlockAccount(String accountNumber) throws AccountNotFoundException {
        return service.unlockAccount(accountNumber);
    }

    public List<Transaction> getTransactionHistory(String accountNumber) throws AccountNotFoundException {
        return service.getTransactionHistory(accountNumber);
    }
}
