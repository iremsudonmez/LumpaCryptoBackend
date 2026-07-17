package com.lumpacrypto.backend.market;

import java.util.List;

// decoupling seam -> ticker engine now, live api later without touching anything else
public interface MarketDataProvider {
    List<PriceQuote> fetchLatestPrices();
}