package com.coffeebank.portal.account.domain;

import com.coffeebank.portal.account.data.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final AccountRepository accountRepository;

    public DataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("--- Initializing CoffeeBank with Data --- ");

        if (accountRepository.count() == 0) {
            Account Tom = new Account("TOM", "Tom Burrows", 25);
            Account Paddy = new Account("PADDY", "Paddy Cunnane", 25);
            Account Eric = new Account("ERIC", "Eric Dao", 25);
            Account Patrick = new Account("PATRICK", "Patrick Doan", 1000);

            accountRepository.save(Tom);
            accountRepository.save(Paddy);
            accountRepository.save(Eric);
            accountRepository.save(Patrick);

            System.out.println("Accounts created.");
        }

    }
}
