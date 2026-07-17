package com.lumpacrypto.backend.trading;

import com.lumpacrypto.backend.common.config.SessionAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    // userId was placed into the request by SessionAuthFilter
    private UUID userId(HttpServletRequest request) {
        return (UUID) request.getAttribute(SessionAuthFilter.USER_ID_ATTR);
    }

    @PostMapping("/orders")
    public TradingDtos.OrderResponse executeOrder(
            @Valid @RequestBody TradingDtos.OrderRequest req, HttpServletRequest request) {
        return tradingService.executeOrder(userId(request), req);
    }

    @GetMapping("/portfolio")
    public TradingDtos.PortfolioDto getPortfolio(HttpServletRequest request) {
        return tradingService.getPortfolio(userId(request));
    }

    @GetMapping("/transactions")
    public List<TradingDtos.TransactionDto> getTransactions(HttpServletRequest request) {
        return tradingService.getPortfolio(userId(request)).recentTransactions();
    }
}