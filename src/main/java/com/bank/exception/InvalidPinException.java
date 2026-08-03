package com.bank.exception;

/**
 * Thrown when the user input PIN is incorrect or does not conform to the 4-digit requirement.
 */
public class InvalidPinException extends Exception {
    public InvalidPinException(String message) {
        super(message);
    }
}
