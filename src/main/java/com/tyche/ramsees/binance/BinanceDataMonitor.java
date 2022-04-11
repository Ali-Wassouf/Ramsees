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
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceDataMonitor {
    public static final int TREND_EMA = 100;
    public static final int MACD_SHORT = 12;
    public static final int MACD_LONG = 26;
    public static final int MACD_SIGNAL_LENGTH = 9;
    public static final Number STOP_GAIN = 0.6;
    public static final Number STOP_LOSS = 0.5;

    private final BinanceDataFetcher binanceDataFetcher;

    private int iteration = 0;
    private boolean minimumBarCountReached = false;

    // Klines
    private BarSeries series;
    MACDIndicator macd;
    EMAIndicator macdSignal;
    EMAIndicator trendEma;
    private Strategy strategy;

    @PostConstruct
    public void init() {
        // Init
        series = new BaseBarSeriesBuilder().withMaxBarCount(TREND_EMA).withName("BINANCE_ETHBUSD").build();
        var closePrice = new ClosePriceIndicator(series);
        macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        macdSignal = new EMAIndicator(macd, MACD_SIGNAL_LENGTH);
        trendEma = new EMAIndicator(closePrice, TREND_EMA);

        // Strategy
        var buyingRule = new OverIndicatorRule(closePrice, trendEma)
            .and(new CrossedUpIndicatorRule(macd, macdSignal));

        var sellingRule = new CrossedDownIndicatorRule(macd, macdSignal)
            .or(new StopGainRule(closePrice, STOP_GAIN))
            .or(new StopLossRule(closePrice, STOP_LOSS));
        strategy = new BaseStrategy(buyingRule, sellingRule);
    }

    public void updateKlines() {
        log.info("-----------------------------------------------");
        log.info("Iteration {}", ++iteration);

        List<KlineResponseDTO> klineList;
        if(!minimumBarCountReached){
            // Fetch the latest initial amount of bars in one request
            klineList =
                binanceDataFetcher.fetchLatestKline("ETHBUSD", "1m", TREND_EMA);
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
        log.info("trendEma: " + trendEma.getValue(series.getEndIndex()));
        log.info("macd: " + macd.getValue(series.getEndIndex()));
        log.info("macdSignal: " + macdSignal.getValue(series.getEndIndex()));
    }

    public double getLastBarValue() {
        return series.getLastBar().getClosePrice().doubleValue();
    }
}
