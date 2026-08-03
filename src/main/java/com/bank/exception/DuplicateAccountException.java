package com.bank.exception;

/**
 * Thrown when attempting to register a customer account with details 
 * (like phone or email) that already exist in the system.
 */
public class DuplicateAccountException extends Exception {
    public DuplicateAccountException(String message) {
        super(message);
    }
}
