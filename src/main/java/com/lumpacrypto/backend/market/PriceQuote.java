package com.lumpacrypto.backend.market;

import java.math.BigDecimal;
import java.time.Instant;

// runtime price value -> cached in redis, snapshotted into postgres
public record PriceQuote(String symbol, BigDecimal price, Instant quotedAt) {}