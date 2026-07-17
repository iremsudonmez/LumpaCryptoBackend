package com.lumpacrypto.backend.market;

import com.lumpacrypto.backend.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final MarketDataService marketDataService;
    private final PriceSnapshotRepository snapshotRepository;

    public MarketController(MarketDataService marketDataService,
                            PriceSnapshotRepository snapshotRepository) {
        this.marketDataService = marketDataService;
        this.snapshotRepository = snapshotRepository;
    }

    @GetMapping("/prices")
    public List<PriceQuote> getPrices() {
        return marketDataService.getLatestPrices();
    }

    @GetMapping("/prices/{symbol}")
    public PriceQuote getPrice(@PathVariable String symbol) {
        return marketDataService.getLatestPrice(symbol.toUpperCase());
    }

    // last 100 snapshots oldest-first -> ready for charting
    @GetMapping("/prices/{symbol}/history")
    public List<PriceQuote> getHistory(@PathVariable String symbol) {
        String upper = symbol.toUpperCase();
        if (!MarketDataService.SUPPORTED_SYMBOLS.contains(upper))
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "UNSUPPORTED_SYMBOL", "Symbol not supported: " + upper);
        return snapshotRepository.findTop100BySymbolOrderByCapturedAtDesc(upper)
                .stream()
                .map(s -> new PriceQuote(s.getSymbol(), s.getPrice(), s.getCapturedAt()))
                .sorted(Comparator.comparing(PriceQuote::quotedAt))
                .toList();
    }
}