package com.bank.model;

/**
 * Represents a banking customer associated with a specific financial account.
 */
public class Customer extends User {
    private static final long serialVersionUID = 1L;

    private Account account;

    public Customer(String id, String name, String email, String phone, String address, int age, String gender, Account account) {
        super(id, name, email, phone, address, age, gender, Role.CUSTOMER);
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
