package com.bank.model;

/**
 * Represents the access and authority roles in the system.
 * <ul>
 *     <li>{@link #ADMIN} - Responsible for administrative tasks like viewing logs, unlocking accounts, and generating reports.</li>
 *     <li>{@link #CUSTOMER} - The basic client profile that holds accounts, performs transactions, and updates profiles.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    CUSTOMER
}
