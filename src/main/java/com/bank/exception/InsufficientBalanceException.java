package com.bank.exception;

/**
 * Thrown when a withdrawal or transfer request exceeds the available balance 
 * or would cause the balance to fall below the minimum threshold.
 */
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
