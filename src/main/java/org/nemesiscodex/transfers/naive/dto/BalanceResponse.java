package org.nemesiscodex.transfers.naive.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(UUID userId, BigDecimal amount) {
}
