package com.bank.utility;

import java.util.regex.Pattern;

/**
 * Utility class containing common validators for email, phone number, and inputs.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{4}$");

    /**
     * Validates that the customer name is not empty and contains only letters and spaces.
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.matches("^[a-zA-Z\\s]{2,50}$");
    }

    /**
     * Validates email string against standard RFC 5322 regex.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number contains exactly 10 digits.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates PIN contains exactly 4 digits.
     */
    public static boolean isValidPin(String pin) {
        return pin != null && PIN_PATTERN.matcher(pin).matches();
    }

    /**
     * Validates the age of the customer (must be between 18 and 100).
     */
    public static boolean isValidAge(int age) {
        return age >= 18 && age <= 100;
    }
}
