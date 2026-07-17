package com.lumpacrypto.backend.trading;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldingRepository extends JpaRepository<Holding, UUID> {
    Optional<Holding> findByWalletIdAndSymbol(UUID walletId, String symbol);
    List<Holding> findByWalletId(UUID walletId);
}