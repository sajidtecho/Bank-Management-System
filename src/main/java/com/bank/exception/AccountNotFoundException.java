package com.bank.exception;

/**
 * Thrown when an account lookup by account number or customer identifiers fails.
 */
public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
