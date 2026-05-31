package org.coffeebank.service;

import org.coffeebank.models.Account;
import org.coffeebank.models.Customer;
import org.coffeebank.models.Transaction;
import org.coffeebank.models.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankingService {
    private final Map<UUID, Account> accountRegistry = new HashMap<>();
    private final Map<String, Customer> customerRegistry = new HashMap<>();
    public void registerCustomer(Customer customer) {
        if (customer == null ||
            customer.getEmail() == null ||
            customerRegistry.containsKey(customer.getEmail())) {
            return;
        }

        customerRegistry.put(customer.getEmail(), customer);
    }

    public Customer getCustomerByEmail(String email) {
        if (email == null || !customerRegistry.containsKey((email))) {
            return null;
        }
        return customerRegistry.get(email);
    }

    public void registerAccount(Account account) {
        accountRegistry.put(account.getId(), account);
    }
    public void transfer(UUID fromId, UUID toId, BigDecimal amount) {
        if (!accountRegistry.containsKey(fromId) || !accountRegistry.containsKey(toId)) {
            return; // Or throw an exception
        }

        Account fromAccount = accountRegistry.get(fromId);
        Account toAccount = accountRegistry.get(toId);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            return;
        }

        // Ensure from has enough balance to transfer
        fromAccount.subtractBalance(amount);
        toAccount.addBalance(amount);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                fromAccount.getId(),
                toAccount.getId(),
                TransactionType.TRANSFER,
                amount,
                "Transfer",
                LocalDateTime.now());

        fromAccount.addTransaction(transaction);
        toAccount.addTransaction(transaction);

    }
    public void deposit(UUID accountId, BigDecimal amount) {
        if (!accountRegistry.containsKey(accountId)) {
            return;
        }

        Account account = accountRegistry.get(accountId);

        var transaction = new Transaction(
                UUID.randomUUID(),
                null,
                accountId,
                TransactionType.DEPOSIT,
                amount,
                "DEPOSIT",
                LocalDateTime.now()
        );
        account.addBalance(amount);
        account.addTransaction(transaction);

    }
    public boolean reconcile(UUID accountId) {
        if (!accountRegistry.containsKey(accountId)) {
            return false;
        }
        var account = accountRegistry.get(accountId);
        BigDecimal calculatedBalance = BigDecimal.ZERO;

        for (Transaction tx : account.getHistory())
        {
            // toAccountId means receiver (credit)
            if (accountId.equals(tx.toAccountId())) {
                calculatedBalance = calculatedBalance.add(tx.amount());
            }
            // fromAccountId means sender (debit)
            else if (accountId.equals(tx.fromAccountId())) {
                calculatedBalance = calculatedBalance.subtract(tx.amount());
            }
        }
        return calculatedBalance.compareTo(account.getBalance()) == 0;
    }
}
