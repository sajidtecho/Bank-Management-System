package com.bank.exception;

/**
 * Thrown when attempting to login or execute operations on a locked customer account.
 */
public class AccountLockedException extends Exception {
    public AccountLockedException(String message) {
        super(message);
    }
}
