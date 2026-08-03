package com.bank.utility;

import java.util.Set;

/**
 * Utility class to generate unique 10-digit account numbers sequentially.
 */
public class AccountNumberGenerator {
    
    private static final long BASE_ACCOUNT_NUMBER = 1000000000L;

    /**
     * Generates a unique 10-digit account number that does not exist in the system.
     *
     * @param existingNumbers Set of currently occupied account numbers.
     * @return A unique 10-digit numeric String.
     */
    public synchronized static String generate(Set<String> existingNumbers) {
        long nextNum = BASE_ACCOUNT_NUMBER + existingNumbers.size() + 1;
        while (existingNumbers.contains(String.valueOf(nextNum))) {
            nextNum++;
        }
        return String.valueOf(nextNum);
    }
}
