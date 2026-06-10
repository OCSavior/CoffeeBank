package org.coffeebank.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
    UUID id,
    TransactionType type,
    BigDecimal amount,
    String description,
    LocalDateTime timestamp
) {}
