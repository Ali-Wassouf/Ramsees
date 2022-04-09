package com.tyche.ramsees.binance;

import com.tyche.ramsees.Step;
import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.utilities.TradingManager;
import java.time.ZonedDateTime;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceDataFetchingService {

//    private SlotsConfigProps slotsConfigProps;
    public static final int SHORT_SEQUENCE = 7;
    public static final int LONG_SEQUENCE = 14;
    public static final Number STOP_LOSS = 0.5;
    public static final Number STOP_GAIN = 1;

    private final DataFetcherBinanceImpl dataFetcherBinanceImpl;
    private final TradingManager tradingManager = new TradingManager();
    private int iteration = 0;

    // Klines
    BarSeries series;
    ClosePriceIndicator closePrice;
    SMAIndicator shortSma;
    SMAIndicator longSma;
    Rule buyingRule;
    Rule sellingRule;
    Strategy strategy;

    @PostConstruct
    public void init() {
        series = new BaseBarSeriesBuilder().withMaxBarCount(LONG_SEQUENCE).withName("BINANCE_ETHBUSD").build();
        closePrice = new ClosePriceIndicator(series);
        shortSma = new SMAIndicator(closePrice, SHORT_SEQUENCE);
        longSma = new SMAIndicator(closePrice, LONG_SEQUENCE);
        buyingRule = new CrossedUpIndicatorRule(shortSma, longSma);
        sellingRule = new CrossedDownIndicatorRule(shortSma, longSma)
            .or(new StopLossRule(closePrice, STOP_LOSS))
            .or(new StopGainRule(closePrice, STOP_GAIN));
        strategy = new BaseStrategy(buyingRule, sellingRule);
    }

    public void getKlines() {
        log.info("-----------------------------------------------");
        log.info("Iteration {}", ++iteration);

        var klineList =
            dataFetcherBinanceImpl.fetchLatestKline("ETHBUSD", "1m");

        for(KlineResponseDTO k : klineList) {
            series.addBar(
                ZonedDateTime.now(),
                Double.valueOf(k.open),
                Double.valueOf(k.high),
                Double.valueOf(k.low),
                Double.valueOf(k.close),
                Double.valueOf(k.volume)
            );
        }

        logStatus();

        int endIndex = series.getEndIndex();
        if (strategy.shouldEnter(endIndex)) {
            // Entering...
            if (tradingManager.getStep() == Step.BUY_NEXT) {
                tradingManager.buy(series.getLastBar().getClosePrice().doubleValue());
            }
        } else if (strategy.shouldExit(endIndex)) {
            // Exiting...
            if (tradingManager.getStep() == Step.SELL_NEXT) {
                tradingManager.sell(series.getLastBar().getClosePrice().doubleValue());
            }
        }
    }

    private void logStatus() {
        log.info(series.getLastBar().toString());
        log.info("Current budget is {}", tradingManager.getBudget());
        log.info("Current Eth is {}", tradingManager.getEth());
        if(series.getBarCount() == LONG_SEQUENCE){
            log.info("Short SMA {}", shortSma.getValue(SHORT_SEQUENCE - 1));
            log.info("Long SMA {}", longSma.getValue(LONG_SEQUENCE - 1));
        }
    }
}
