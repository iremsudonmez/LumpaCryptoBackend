package com.lumpacrypto.backend.trading;

import com.lumpacrypto.backend.auth.Wallet;
import com.lumpacrypto.backend.auth.WalletRepository;
import com.lumpacrypto.backend.common.error.ApiException;
import com.lumpacrypto.backend.market.MarketDataService;
import com.lumpacrypto.backend.market.PriceQuote;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class TradingService {

    private final WalletRepository walletRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final MarketDataService marketDataService;

    public TradingService(WalletRepository walletRepository,
                          HoldingRepository holdingRepository,
                          TransactionRepository transactionRepository,
                          MarketDataService marketDataService) {
        this.walletRepository = walletRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.marketDataService = marketDataService;
    }

    // whole trade commits or rolls back as one unit -> transactional integrity rule
    @Transactional
    public TradingDtos.OrderResponse executeOrder(UUID userId, TradingDtos.OrderRequest req) {
        String symbol = req.symbol().toUpperCase();
        PriceQuote quote = marketDataService.getLatestPrice(symbol); // validates symbol + cache

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Wallet missing"));

        return "BUY".equals(req.side())
                ? buy(userId, wallet, symbol, quote, req.amount())
                : sell(userId, wallet, symbol, quote, req.amount());
    }

    private TradingDtos.OrderResponse buy(UUID userId, Wallet wallet, String symbol,
                                          PriceQuote quote, BigDecimal fiatToSpend) {
        if (fiatToSpend.compareTo(wallet.getFiatBalance()) > 0)
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "INSUFFICIENT_FUNDS", "Insufficient funds to complete this trade");

        // quantity rounded DOWN to 8dp -> never mint value from rounding
        BigDecimal quantity = fiatToSpend.divide(quote.price(), 8, RoundingMode.DOWN);

        wallet.setFiatBalance(wallet.getFiatBalance().subtract(fiatToSpend));
        walletRepository.save(wallet);

        Holding holding = holdingRepository.findByWalletIdAndSymbol(wallet.getId(), symbol)
                .orElseGet(() -> new Holding(wallet.getId(), symbol, BigDecimal.ZERO));
        holding.setQuantity(holding.getQuantity().add(quantity));
        holdingRepository.save(holding);

        Transaction tx = transactionRepository.save(new Transaction(
                userId, symbol, "BUY", quantity, quote.price(), fiatToSpend));

        return toOrderResponse(tx, wallet.getFiatBalance());
    }

    private TradingDtos.OrderResponse sell(UUID userId, Wallet wallet, String symbol,
                                           PriceQuote quote, BigDecimal quantityToSell) {
        Holding holding = holdingRepository.findByWalletIdAndSymbol(wallet.getId(), symbol)
                .orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
                        "INSUFFICIENT_HOLDINGS", "You do not own this asset"));

        if (quantityToSell.compareTo(holding.getQuantity()) > 0)
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "INSUFFICIENT_HOLDINGS", "Sell quantity exceeds owned quantity");

        BigDecimal fiatReceived = quantityToSell.multiply(quote.price())
                .setScale(2, RoundingMode.HALF_UP);

        holding.setQuantity(holding.getQuantity().subtract(quantityToSell));
        if (holding.getQuantity().compareTo(BigDecimal.ZERO) == 0)
            holdingRepository.delete(holding);
        else
            holdingRepository.save(holding);

        wallet.setFiatBalance(wallet.getFiatBalance().add(fiatReceived));
        walletRepository.save(wallet);

        Transaction tx = transactionRepository.save(new Transaction(
                userId, symbol, "SELL", quantityToSell, quote.price(), fiatReceived));

        return toOrderResponse(tx, wallet.getFiatBalance());
    }

    private TradingDtos.OrderResponse toOrderResponse(Transaction tx, BigDecimal newBalance) {
        return new TradingDtos.OrderResponse(
                tx.getSymbol(), tx.getSide(), tx.getQuantity(),
                tx.getExecutionPrice(), tx.getFiatAmount(), newBalance, tx.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public TradingDtos.PortfolioDto getPortfolio(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Wallet missing"));

        List<TradingDtos.HoldingDto> holdings = holdingRepository.findByWalletId(wallet.getId())
                .stream()
                .map(h -> {
                    PriceQuote q = marketDataService.getLatestPrice(h.getSymbol());
                    BigDecimal value = h.getQuantity().multiply(q.price())
                            .setScale(2, RoundingMode.HALF_UP);
                    return new TradingDtos.HoldingDto(h.getSymbol(), h.getQuantity(), q.price(), value);
                })
                .toList();

        BigDecimal totalValue = holdings.stream()
                .map(TradingDtos.HoldingDto::value)
                .reduce(wallet.getFiatBalance(), BigDecimal::add);

        List<TradingDtos.TransactionDto> recent = transactionRepository
                .findTop20ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(t -> new TradingDtos.TransactionDto(
                        t.getId(), t.getSymbol(), t.getSide(), t.getQuantity(),
                        t.getExecutionPrice(), t.getFiatAmount(), t.getCreatedAt()))
                .toList();

        return new TradingDtos.PortfolioDto(
                wallet.getFiatBalance(), holdings, totalValue, recent);
    }
}