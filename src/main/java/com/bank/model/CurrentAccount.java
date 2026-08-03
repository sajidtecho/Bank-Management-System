package com.bank.model;

/**
 * Represents a Current Account. Requires a minimum balance of 2000.0.
 */
public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;

    private static final double MIN_BALANCE = 2000.0;

    public CurrentAccount(String accountNumber, String pin, double balance) {
        super(accountNumber, pin, balance, AccountType.CURRENT);
    }

    @Override
    public double getMinBalance() {
        return MIN_BALANCE;
    }
}
