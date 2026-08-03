package com.bank.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing a generic bank account.
 * Enforces the Template Method Pattern / Strategy for minimum balance requirements.
 */
public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountNumber;
    private String pin;
    private double balance;
    private AccountType accountType;
    private LocalDateTime dateCreated;
    private boolean locked;
    private int failedAttempts;
    private List<Transaction> transactions;

    protected Account(String accountNumber, String pin, double balance, AccountType accountType) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.accountType = accountType;
        this.dateCreated = LocalDateTime.now();
        this.locked = false;
        this.failedAttempts = 0;
        this.transactions = new ArrayList<>();
    }

    /**
     * @return The minimum balance required for this type of account.
     */
    public abstract double getMinBalance();

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction(Transaction transaction) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }
}
