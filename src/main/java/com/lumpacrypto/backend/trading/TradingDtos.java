package com.lumpacrypto.backend.trading;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TradingDtos {

    public record OrderRequest(
            @NotBlank String symbol,
            @NotNull @Pattern(regexp = "BUY|SELL") String side,
            @NotNull @Positive BigDecimal amount) {}

    // matches frontend OrderResponse in types.ts
    public record OrderResponse(
            String symbol, String side, BigDecimal quantity,
            BigDecimal executionPrice, BigDecimal fiatAmount,
            BigDecimal fiatBalance, Instant createdAt) {}

    public record HoldingDto(
            String symbol, BigDecimal quantity, BigDecimal price, BigDecimal value) {}

    public record TransactionDto(
            UUID id, String symbol, String side, BigDecimal quantity,
            BigDecimal executionPrice, BigDecimal fiatAmount, Instant createdAt) {}

    // matches frontend PortfolioDto in types.ts
    public record PortfolioDto(
            BigDecimal fiatBalance,
            List<HoldingDto> holdings,
            BigDecimal totalValue,
            List<TransactionDto> recentTransactions) {}
}