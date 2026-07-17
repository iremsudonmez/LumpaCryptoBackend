package com.lumpacrypto.backend.market;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, UUID> {
}