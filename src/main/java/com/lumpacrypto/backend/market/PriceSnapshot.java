package com.lumpacrypto.backend.market;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "price_snapshots")
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    protected PriceSnapshot() {}

    public PriceSnapshot(String symbol, BigDecimal price, Instant capturedAt) {
        this.symbol = symbol;
        this.price = price;
        this.capturedAt = capturedAt;
    }

    public String getSymbol() { return symbol; }
    public BigDecimal getPrice() { return price; }
    public Instant getCapturedAt() { return capturedAt; }
}