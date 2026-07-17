package com.lumpacrypto.backend.auth;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "fiat_balance", nullable = false)
    private BigDecimal fiatBalance;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Wallet() {}

    public Wallet(UUID userId, BigDecimal fiatBalance) {
        this.userId = userId;
        this.fiatBalance = fiatBalance;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public BigDecimal getFiatBalance() { return fiatBalance; }

    public void setFiatBalance(BigDecimal fiatBalance) {
        this.fiatBalance = fiatBalance;
        this.updatedAt = Instant.now();
    }
}