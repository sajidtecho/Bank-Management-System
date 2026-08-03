package com.bank.app;

import com.bank.controller.BankController;
import com.bank.menu.ConsoleMenu;
import com.bank.repository.AccountRepository;
import com.bank.repository.AccountRepositoryImpl;
import com.bank.service.BankService;
import com.bank.service.BankServiceImpl;

/**
 * Main entrance point of the application. Bootstraps the layers.
 */
public class Main {
    public static void main(String[] args) {
        // 1. Initialize data layer (loads from flat files automatically)
        AccountRepository repository = new AccountRepositoryImpl();

        // 2. Initialize service layer
        BankService service = new BankServiceImpl(repository);

        // 3. Initialize controller layer
        BankController controller = new BankController(service);

        // 4. Initialize and start UI
        ConsoleMenu menu = new ConsoleMenu(controller);
        
        // Boot Menu
        menu.start();
    }
}
