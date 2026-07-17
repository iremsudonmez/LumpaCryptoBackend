package com.lumpacrypto.backend.trading;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "holdings")
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Holding() {}

    public Holding(UUID walletId, String symbol, BigDecimal quantity) {
        this.walletId = walletId;
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public UUID getWalletId() { return walletId; }
    public String getSymbol() { return symbol; }
    public BigDecimal getQuantity() { return quantity; }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        this.updatedAt = Instant.now();
    }
}