package com.bank.exception;

/**
 * Custom exception thrown when a transaction amount is invalid 
 * (e.g., negative, zero, or below minimum balance requirements).
 */
public class InvalidAmountException extends Exception {
    
    public InvalidAmountException(String message) {
        super(message);
    }
}
