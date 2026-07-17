package com.lumpacrypto.backend.market;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/prices")
    public List<PriceQuote> getPrices() {
        return marketDataService.getLatestPrices();
    }

    @GetMapping("/prices/{symbol}")
    public PriceQuote getPrice(@PathVariable String symbol) {
        return marketDataService.getLatestPrice(symbol.toUpperCase());
    }
}