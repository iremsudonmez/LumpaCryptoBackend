package com.lumpacrypto.backend.market;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// simulates realistic price movement -> random walk around last value
@Component
public class TickerEngineProvider implements MarketDataProvider {

    private final Map<String, BigDecimal> lastPrices = new ConcurrentHashMap<>(Map.of(
            "BTC", new BigDecimal("65000.00"),
            "ETH", new BigDecimal("3200.00"),
            "SOL", new BigDecimal("145.00"),
            "XRP", new BigDecimal("0.52")));

    @Override
    public List<PriceQuote> fetchLatestPrices() {
        Instant now = Instant.now();
        return lastPrices.entrySet().stream()
                .map(e -> {
                    // +/- 0.5% move each tick -> feels like a real market
                    double factor = 1 + ThreadLocalRandom.current().nextDouble(-0.005, 0.005);
                    BigDecimal next = e.getValue()
                            .multiply(BigDecimal.valueOf(factor))
                            .setScale(2, RoundingMode.HALF_UP);
                    lastPrices.put(e.getKey(), next);
                    return new PriceQuote(e.getKey(), next, now);
                })
                .toList();
    }
}