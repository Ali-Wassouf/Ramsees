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
    private final RamseesBaseStrategy strategy = new MacDBasedStrategy();

    private int iteration = 0;

    @PostConstruct
    public void init() {
        strategy.build();
    }

    public void updateKlines() {
        log.info("-----------------------------------------------");
        log.info("Iteration {}", ++iteration);

        strategy.fetchData(binanceDataFetcher);
        strategy.logStatus();
    }

    public boolean shouldEnter() {
        return strategy.shouldEnter();
    }

    public boolean shouldExit() {
        return strategy.shouldExit();
    }

    public double getLastBarValue() {
        return strategy.getLastBarValue();
    }
}
