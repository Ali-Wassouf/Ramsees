package com.tyche.ramsees.binance.job;

import com.tyche.ramsees.binance.BinanceDataFetchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinanceDataFetcherJob {

    private final BinanceDataFetchingService binanceDataFetchingService;

    @Scheduled(fixedRate = 60000)
    public void runJob() {
        binanceDataFetchingService.getPairPrice();
    }
}
