package com.lumpacrypto.backend.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lumpacrypto.backend.common.error.ApiException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MarketDataService {

    public static final Set<String> SUPPORTED_SYMBOLS = Set.of("BTC", "ETH", "SOL", "XRP");
    private static final String KEY_PREFIX = "price:";

    private final MarketDataProvider provider;
    private final PriceSnapshotRepository snapshotRepository;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    public MarketDataService(MarketDataProvider provider,
                             PriceSnapshotRepository snapshotRepository,
                             StringRedisTemplate redis) {
        this.provider = provider;
        this.snapshotRepository = snapshotRepository;
        this.redis = redis;
    }

    // assignment rule -> refresh cache every 15 seconds, persist history to postgres
    @Scheduled(fixedRate = 15_000)
    public void refreshPrices() {
        List<PriceQuote> quotes = provider.fetchLatestPrices();
        for (PriceQuote q : quotes) {
            try {
                redis.opsForValue().set(KEY_PREFIX + q.symbol(), mapper.writeValueAsString(q));
            } catch (JsonProcessingException e) {
                // skip this symbol on serialization trouble -> next tick will retry
            }
            snapshotRepository.save(new PriceSnapshot(q.symbol(), q.price(), q.quotedAt()));
        }
    }

    // reads come from redis only -> low-latency delivery requirement
    public List<PriceQuote> getLatestPrices() {
        List<PriceQuote> result = new ArrayList<>();
        for (String symbol : SUPPORTED_SYMBOLS) {
            String json = redis.opsForValue().get(KEY_PREFIX + symbol);
            if (json == null)
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "PRICE_UNAVAILABLE", "Prices are warming up, try again shortly");
            try {
                result.add(mapper.readValue(json, PriceQuote.class));
            } catch (JsonProcessingException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_ERROR", "Corrupt price cache");
            }
        }
        result.sort((a, b) -> a.symbol().compareTo(b.symbol()));
        return result;
    }

    public PriceQuote getLatestPrice(String symbol) {
        if (!SUPPORTED_SYMBOLS.contains(symbol))
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "UNSUPPORTED_SYMBOL", "Symbol not supported: " + symbol);
        String json = redis.opsForValue().get(KEY_PREFIX + symbol);
        if (json == null)
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "PRICE_UNAVAILABLE", "Price not available yet");
        try {
            return mapper.readValue(json, PriceQuote.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "INTERNAL_ERROR", "Corrupt price cache");
        }
    }
}