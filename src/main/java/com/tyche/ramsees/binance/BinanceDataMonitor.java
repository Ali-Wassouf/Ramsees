package com.tyche.ramsees.binance;

import com.tyche.ramsees.strategies.MacDBasedStrategy;
import com.tyche.ramsees.strategies.RamseesBaseStrategy;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceDataMonitor {

    private final BinanceDataFetcher binanceDataFetcher;
    // Easy switch between strategies
    private RamseesBaseStrategy strategy;

    private int iteration = 0;

    @PostConstruct
    public void init() {
        strategy = new MacDBasedStrategy(binanceDataFetcher.getSeries());
    }

    public void updateKlines() {
        log.info("-----------------------------------------------");
        log.info("Iteration {}", ++iteration);

        strategy.logStatus(binanceDataFetcher.getLatestBar(), binanceDataFetcher.getEndIndex());
    }

    public boolean shouldEnter() {
        return strategy.shouldEnter(binanceDataFetcher.getEndIndex());
    }

    public boolean shouldExit() {
        return strategy.shouldExit(binanceDataFetcher.getEndIndex());
    }

    public double getLastBarValue() {
        return binanceDataFetcher.getLastBarValue();
    }
}
