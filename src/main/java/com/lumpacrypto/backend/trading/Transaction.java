package com.lumpacrypto.backend.trading;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String side;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "execution_price", nullable = false)
    private BigDecimal executionPrice;

    @Column(name = "fiat_amount", nullable = false)
    private BigDecimal fiatAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Transaction() {}

    public Transaction(UUID userId, String symbol, String side,
                       BigDecimal quantity, BigDecimal executionPrice, BigDecimal fiatAmount) {
        this.userId = userId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.fiatAmount = fiatAmount;
    }

    public UUID getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getExecutionPrice() { return executionPrice; }
    public BigDecimal getFiatAmount() { return fiatAmount; }
    public Instant getCreatedAt() { return createdAt; }
}