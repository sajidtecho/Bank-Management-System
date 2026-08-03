package com.bank.repository;

import com.bank.model.Admin;
import com.bank.model.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Interface representing standard CRUD database access operations on accounts, transactions, and admin files.
 */
public interface AccountRepository {

    /**
     * Loads all admins, customers, and transactions from resources directory files.
     */
    void loadData();

    /**
     * Saves all in-memory lists back to resources directory files.
     */
    void saveData();

    List<Customer> getAllCustomers();

    Optional<Customer> getCustomerByAccountNumber(String accountNumber);

    Optional<Customer> getCustomerByPhone(String phone);

    Optional<Customer> getCustomerByEmail(String email);

    void saveCustomer(Customer customer);

    boolean deleteCustomer(String accountNumber);

    List<Admin> getAllAdmins();

    Optional<Admin> getAdminByUsername(String username);

    void saveAdmin(Admin admin);
}
