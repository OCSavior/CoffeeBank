package org.coffeebank.service;

import org.coffeebank.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class BankingService {
    private final Map<UUID, Account> accountRegistry = new HashMap<>();
    private final Map<String, Customer> customerRegistry = new HashMap<>();
    private final Map<UUID, Transaction> transactionRegistry = new HashMap<>();
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
            return;
        }

        Account fromAccount = accountRegistry.get(fromId);
        Account toAccount = accountRegistry.get(toId);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            return;
        }

        fromAccount.subtractBalance(amount);
        toAccount.addBalance(amount);

        UUID txId = UUID.randomUUID();
        Transaction transaction = new Transaction(txId, TransactionType.TRANSFER, amount, "Transfer", LocalDateTime.now());
        transactionRegistry.put(txId, transaction);

        fromAccount.addLedgerEntry(new LedgerEntry(UUID.randomUUID(), txId, fromId, EntryDirection.DEBIT, amount, LocalDateTime.now()));
        toAccount.addLedgerEntry(new LedgerEntry(UUID.randomUUID(), txId, toId, EntryDirection.CREDIT, amount, LocalDateTime.now()));
    }

    public void deposit(UUID accountId, BigDecimal amount) {
        if (!accountRegistry.containsKey(accountId)) {
            return;
        }

        Account account = accountRegistry.get(accountId);

        UUID txId = UUID.randomUUID();
        Transaction transaction = new Transaction(txId, TransactionType.DEPOSIT, amount, "Deposit", LocalDateTime.now());
        transactionRegistry.put(txId, transaction);

        account.addBalance(amount);
        account.addLedgerEntry(new LedgerEntry(UUID.randomUUID(), txId, accountId, EntryDirection.CREDIT, amount, LocalDateTime.now()));
    }

    public List<AccountSummary> getAccountDetails(Customer customer) {
        return customer.getAccounts().stream()
                .map(a -> new AccountSummary(a.getType().name(), a.getBalance(), a.getHistory()))
                .toList();
    }

    public boolean reconcile(UUID accountId) {
        if (!accountRegistry.containsKey(accountId)) {
            return false;
        }
        var account = accountRegistry.get(accountId);
        BigDecimal calculatedBalance = BigDecimal.ZERO;

        for (LedgerEntry le : account.getHistory())
        {
            if (le.direction().equals(EntryDirection.CREDIT)) {
                calculatedBalance = calculatedBalance.add(le.amount());
            }
            else if (le.direction().equals(EntryDirection.DEBIT)) {
                calculatedBalance = calculatedBalance.subtract(le.amount());
            }
        }
        return calculatedBalance.compareTo(account.getBalance()) == 0;
    }

    public void initiateTransfer(Customer customer, Scanner scanner) {
        while (true) {
            // Step 1: Select from account
            Map<String, Account> accountOptions = getAccountOptions(customer);
            System.out.println("\nSelect an account to transfer from (or type 'exit' to cancel):");
            for (Map.Entry<String, Account> entry : accountOptions.entrySet()) {
                System.out.println(entry.getKey() + ". " + entry.getValue().getType() + " - $" +
                        entry.getValue().getBalance());
            }

            String fromChoice = scanner.nextLine().trim();
            if (fromChoice.equalsIgnoreCase("exit")) return;

            Account fromAccount = accountOptions.get(fromChoice);
            if (fromAccount == null) {
                System.out.println("Invalid selection. Please try again.");
                continue;
            }

            // Step 2: Build filtered list excluding the from account
            Map<String, Account> toOptions = new LinkedHashMap<>();
            int i = 1;
            for (Account account : customer.getAccounts()) {
                if (!account.getId().equals(fromAccount.getId())) {
                    toOptions.put(String.valueOf(i), account);
                    i++;
                }
            }

            // Step 3: Select to account
            System.out.println("\nSelect an account to transfer to:");
            for (Map.Entry<String, Account> entry : toOptions.entrySet()) {
                System.out.println(entry.getKey() + ". " + entry.getValue().getType() + " - $" +
                        entry.getValue().getBalance());
            }

            String toChoice = scanner.nextLine().trim();
            Account toAccount = toOptions.get(toChoice);
            if (toAccount == null) {
                System.out.println("Invalid selection. Please try again.");
                continue;
            }

            // Step 4: Safety guard
            if (fromAccount.getId().equals(toAccount.getId())) {
                System.out.println("Cannot transfer to the same account.");
                continue;
            }

            // Step 5: Get amount
            System.out.println("How much would you like to transfer from " + fromAccount.getType() + " to " + toAccount.getType() + "?");
            BigDecimal amount;
            try {
                amount = new BigDecimal(scanner.nextLine().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Amount must be greater than zero.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
                continue;
            }

            // Step 6: Execute transfer
            transfer(fromAccount.getId(), toAccount.getId(), amount);
            System.out.println("Transfer successful! New balance for " + fromAccount.getType() + ": $" + fromAccount.getBalance());

            // Step 7: Transfer again?
            System.out.println("Would you like to make another transfer? (yes/no)");
            String again = scanner.nextLine().trim();
            if (!again.equalsIgnoreCase("yes")) return;
        }
    }
    public void initiateDeposit(Customer customer, Scanner scanner) {
        while (true) {
            // Allow the user to pick from list of accounts to deposit
            Map<String, Account> accountOptions = getAccountOptions(customer);
            System.out.println("\nSelect an account to deposit into (or type 'exit' to cancel):");
            for (Map.Entry<String, Account> entry : accountOptions.entrySet()) {
                System.out.println(entry.getKey() + ". " + entry.getValue().getType() + " - $" +
                        entry.getValue().getBalance());
            }

            String choiceIndex = scanner.nextLine().trim();
            if (choiceIndex.equalsIgnoreCase("exit")) return;

            Account selectedAccount = accountOptions.get(choiceIndex);
            if (selectedAccount == null) {
                System.out.println("Invalid selection. Please try again.");
                continue;
            }

            System.out.println("How much would you like to deposit into " + selectedAccount.getType() + "?");
            BigDecimal depositAmount;
            try {
                depositAmount = new BigDecimal(scanner.nextLine().trim());
                if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Amount must be greater than zero.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
                continue;
            }

            deposit(selectedAccount.getId(), depositAmount);
            System.out.println("Deposit successful! New balance: $" + selectedAccount.getBalance());

            System.out.println("Would you like to make another deposit? (yes/no)");
            String again = scanner.nextLine().trim();
            if (!again.equalsIgnoreCase("yes")) return;
        }
    }

    private Map<String, Account> getAccountOptions(Customer customer) {
        Map<String, Account> accountOptions = new LinkedHashMap<>();
        int i = 1;
        for (Account account : customer.getAccounts()) {
            accountOptions.put(String.valueOf(i), account);
            i++;
        }

        return accountOptions;
    }
}
