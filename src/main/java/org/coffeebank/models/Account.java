package org.coffeebank.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

public class Account {
    private final UUID id;
    private AccountType type;
    private BigDecimal balance;
    private List<LedgerEntry> history;

    public Account(AccountType type) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.balance = BigDecimal.ZERO;
        this.history = new ArrayList<>();
    }

    // Getters
    public UUID getId() { return id; }
    public AccountType getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public void addBalance(BigDecimal amount) {
        BigDecimal newBalance = this.balance.add(amount);
        this.balance = newBalance.setScale(2, RoundingMode.HALF_UP);
    }
    public void subtractBalance(BigDecimal amount) {
        BigDecimal newBalance = this.balance.subtract(amount);
        this.balance = newBalance.setScale(2, RoundingMode.HALF_UP);
    }
    public List<LedgerEntry> getHistory() { return history; }
    public void addLedgerEntry(LedgerEntry entry) { this.history.add(entry); }
}