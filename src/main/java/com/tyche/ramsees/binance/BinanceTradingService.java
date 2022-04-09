package com.tyche.ramsees.binance;

import com.tyche.ramsees.Step;
import com.tyche.ramsees.utilities.TradingManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceTradingService {

    private final TradingManager tradingManager;
    private final BinanceDataMonitor binanceDataMonitor;


    public void checkTheMarket() {
        binanceDataMonitor.updateKlines();
        if (binanceDataMonitor.shouldEnter() && tradingManager.getStep() == Step.BUY_NEXT) {
            log.info("Entering the market");
            tradingManager.buy(binanceDataMonitor.getLastBarValue());
        } else if (binanceDataMonitor.shouldExit() && tradingManager.getStep() == Step.SELL_NEXT) {
            log.info("Exiting the market");
            tradingManager.sell(binanceDataMonitor.getLastBarValue());
        }
        log.info("Current budget is {}", tradingManager.getBudget());
        log.info("Current Eth is {}", tradingManager.getEth());
    }
}
