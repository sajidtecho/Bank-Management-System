package com.bank.model;

/**
 * Represents a Savings Account. Requires a minimum balance of 500.0.
 */
public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    
    private static final double MIN_BALANCE = 500.0;

    public SavingsAccount(String accountNumber, String pin, double balance) {
        super(accountNumber, pin, balance, AccountType.SAVINGS);
    }

    @Override
    public double getMinBalance() {
        return MIN_BALANCE;
    }
}
