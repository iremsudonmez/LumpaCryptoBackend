package com.lumpacrypto.backend.trading;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
}