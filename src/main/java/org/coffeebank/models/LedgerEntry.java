package org.coffeebank.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LedgerEntry (
        UUID id,
        UUID transactionId,
        UUID accountId,
        EntryDirection direction,
        BigDecimal amount,
        LocalDateTime timestamp
) {}



