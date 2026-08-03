package com.bank.model;

/**
 * Represents an administrator who manages customer accounts and system data.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    public Admin(String id, String name, String email, String phone, String address, String username, String password) {
        super(id, name, email, phone, address, 0, "N/A", Role.ADMIN);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
