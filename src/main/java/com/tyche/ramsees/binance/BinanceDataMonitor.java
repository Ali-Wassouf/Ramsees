package com.tyche.ramsees.binance;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import java.time.ZonedDateTime;
import java.util.List;
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
    private boolean minimumBarCountReached = false;

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

        List<KlineResponseDTO> klineList;
        if(!minimumBarCountReached){
            // Fetch the latest LONG_SEQUENCE amount of bars in one request
            klineList =
                binanceDataFetcher.fetchLatestKline("ETHBUSD", "1m", LONG_SEQUENCE);
            minimumBarCountReached = true;
        } else {
            klineList =
                binanceDataFetcher.fetchLatestKline("ETHBUSD", "1m", 1);
        }


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

    public boolean shouldEnter() {
        return strategy.shouldEnter(series.getEndIndex());
    }

    public boolean shouldExit() {
        return strategy.shouldExit(series.getEndIndex());
    }

    private void logStatus() {
        log.info("Current price: " + series.getLastBar().getClosePrice());
        log.info("shortSma: " + shortSma.getValue(series.getEndIndex()));
        log.info("longSma: " + longSma.getValue(series.getEndIndex()));
    }

    public double getLastBarValue() {
        return series.getLastBar().getClosePrice().doubleValue();
    }
}
