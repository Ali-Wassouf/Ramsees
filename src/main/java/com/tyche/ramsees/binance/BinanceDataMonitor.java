package com.tyche.ramsees.binance;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import java.time.ZonedDateTime;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseStrategy;
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
public class BinanceDataMonitor {

    public static final int SHORT_SEQUENCE = 7;
    public static final int LONG_SEQUENCE = 14;
    public static final Number STOP_LOSS = 0.5;
    public static final Number STOP_GAIN = 1;

    private final BinanceDataFetcher binanceDataFetcher;

    private int iteration = 0;

    // Klines
    private BarSeries series;
    private SMAIndicator shortSma;
    private SMAIndicator longSma;
    private Strategy strategy;

    @PostConstruct
    public void init() {
        series = new BaseBarSeriesBuilder().withMaxBarCount(LONG_SEQUENCE).withName("BINANCE_ETHBUSD").build();
        var closePrice = new ClosePriceIndicator(series);
        shortSma = new SMAIndicator(closePrice, SHORT_SEQUENCE);
        longSma = new SMAIndicator(closePrice, LONG_SEQUENCE);
        var buyingRule = new CrossedUpIndicatorRule(shortSma, longSma);
        var sellingRule = new CrossedDownIndicatorRule(shortSma, longSma)
            .or(new StopLossRule(closePrice, STOP_LOSS))
            .or(new StopGainRule(closePrice, STOP_GAIN));
        strategy = new BaseStrategy(buyingRule, sellingRule);
    }

    public void updateKlines() {
        log.info("-----------------------------------------------");
        log.info("Iteration {}", ++iteration);

        var klineList =
            binanceDataFetcher.fetchLatestKline("ETHBUSD", "1m");

        for(KlineResponseDTO k : klineList) {
            series.addBar(
                ZonedDateTime.now(),
                Double.valueOf(k.getOpen()),
                Double.valueOf(k.getHigh()),
                Double.valueOf(k.getLow()),
                Double.valueOf(k.getClose()),
                Double.valueOf(k.getVolume())
            );
        }

        logStatus();
    }

    public boolean shouldEnter(){
        return strategy.shouldEnter(series.getEndIndex());
    }

    public boolean shouldExit(){
        return strategy.shouldExit(series.getEndIndex());
    }

    private void logStatus() {
        log.info(series.getLastBar().toString());
        if(series.getBarCount() == LONG_SEQUENCE){
            log.info("Short SMA {}", shortSma.getValue(SHORT_SEQUENCE - 1));
            log.info("Long SMA {}", longSma.getValue(LONG_SEQUENCE - 1));
        }
    }

    public double getLastBarValue() {
        return series.getLastBar().getClosePrice().doubleValue();
    }
}
