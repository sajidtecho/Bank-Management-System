package com.bank.service;

import com.bank.exception.*;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.model.AccountType;

import java.util.List;

/**
 * Service Layer interface detailing all bank business logic rules and operations.
 */
public interface BankService {

    Customer createAccount(String name, int age, String gender, String phone, String email, 
                           String address, AccountType type, String pin, double openingBalance) 
            throws DuplicateAccountException, InvalidAmountException;

    User login(String accountOrUsername, String pinOrPassword, boolean isAdmin) 
            throws AccountNotFoundException, InvalidPinException, AccountLockedException;

    void deposit(String accountNumber, double amount) 
            throws AccountNotFoundException, InvalidAmountException;

    void withdraw(String accountNumber, String pin, double amount) 
            throws AccountNotFoundException, InvalidPinException, InsufficientBalanceException, 
                   InvalidAmountException, AccountLockedException;

    void transfer(String senderAccountNumber, String pin, String receiverAccountNumber, double amount) 
            throws AccountNotFoundException, InvalidPinException, InsufficientBalanceException, 
                   InvalidAmountException, AccountLockedException;

    Customer getProfile(String accountNumber) throws AccountNotFoundException;

    void updateProfile(String accountNumber, String phone, String email, String address) 
            throws AccountNotFoundException;

    void changePin(String accountNumber, String oldPin, String newPin) 
            throws AccountNotFoundException, InvalidPinException;

    boolean deleteAccount(String accountNumber) throws AccountNotFoundException;

    List<Customer> searchCustomers(String query);

    List<Customer> getAllCustomersSorted(String sortBy);

    double getTotalDeposits();

    int getTotalAccountsCount();

    boolean unlockAccount(String accountNumber) throws AccountNotFoundException;

    List<Transaction> getTransactionHistory(String accountNumber) throws AccountNotFoundException;
}
