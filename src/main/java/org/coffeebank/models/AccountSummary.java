package org.coffeebank.models;

import java.math.BigDecimal;
import java.util.List;

public record AccountSummary(
        String accountType,
        BigDecimal balance,
        List<LedgerEntry> history
) {}
