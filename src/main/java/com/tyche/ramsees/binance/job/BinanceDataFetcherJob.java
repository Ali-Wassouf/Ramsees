package com.tyche.ramsees.binance.job;

import com.tyche.ramsees.binance.BinanceTradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinanceDataFetcherJob {

    private final BinanceTradingService binanceTradingService;

    @Scheduled(fixedDelay = 60000)
    public void runJob() {
        binanceTradingService.checkTheMarket();
    }
}
