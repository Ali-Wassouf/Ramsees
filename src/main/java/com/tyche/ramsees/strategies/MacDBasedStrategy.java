package com.tyche.ramsees.strategies;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.binance.BinanceDataFetcher;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

@Slf4j
public class MacDBasedStrategy implements RamseesBaseStrategy {

    private static final int TREND_EMA_LENGTH = 100;
    private static final int MACD_SHORT = 12;
    private static final int MACD_LONG = 26;
    private static final int MACD_SIGNAL_LENGTH = 9;
    private static final Number STOP_GAIN = 0.5;
    private static final Number STOP_LOSS = 0.3;

    private final ClosePriceIndicator closePrice;
    private final MACDIndicator macd;
    private final EMAIndicator macdSignal;
    private final EMAIndicator trendEma;
    private Strategy strategy;


    public MacDBasedStrategy() {
        series = new BaseBarSeriesBuilder()
            .withMaxBarCount(1000)
            .withName("BINANCE_ETHBUSD")
            .build();
        closePrice = new ClosePriceIndicator(series);
        macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        macdSignal = new EMAIndicator(macd, MACD_SIGNAL_LENGTH);
        trendEma = new EMAIndicator(closePrice, TREND_EMA_LENGTH);
    }

    @Override
    public Strategy build() {
        var buyingRule = new CrossedUpIndicatorRule(macd, macdSignal)
            .and((i, tradingRecord) -> (macd.getValue(i).doubleValue() < 0))
            .and(new UnderIndicatorRule(closePrice, trendEma));

        var sellingRule = new StopGainRule(closePrice, STOP_GAIN)
            .or(new StopLossRule(closePrice, STOP_LOSS));

        strategy = new BaseStrategy(buyingRule, sellingRule);
        return strategy;
    }

    @Override
    public void fetchData(BinanceDataFetcher binanceDataFetcher) {

    }

    @Override
    public boolean shouldEnter(int endIndex) {
        return strategy.shouldEnter(endIndex);
    }

    @Override
    public boolean shouldExit(int endIndex) {
        return strategy.shouldExit(endIndex);
    }

    @Override
    public void logStatus(Bar lastBar, int endIndex) {
        log.info("Current price: " + lastBar.getClosePrice());
        log.info("trendEma: " + trendEma.getValue(endIndex));
        log.info("macd: " + macd.getValue(endIndex));
        log.info("macdSignal: " + macdSignal.getValue(endIndex));
    }
}
