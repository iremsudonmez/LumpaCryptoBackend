package com.lumpacrypto.backend.market;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, UUID> {
    List<PriceSnapshot> findTop100BySymbolOrderByCapturedAtDesc(String symbol);
}