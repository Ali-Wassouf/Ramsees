package com.tyche.ramsees.strategies;

import com.tyche.ramsees.binance.BinanceDataFetcher;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class ProductionStrategy implements RamseesBaseStrategy {
  final int TREND_EMA = 100;
  final int MACD_SHORT = 12;
  final int MACD_LONG = 26;
  final int MACD_SIGNAL_LENGTH = 9;
  final Number STOP_GAIN = 0.5;
  final Number STOP_LOSS = 1;

  private BarSeries series;
  private ClosePriceIndicator closePrice;
  private MACDIndicator macd;
  private EMAIndicator macdSignal;
  private EMAIndicator trendEma;
  private Strategy strategy;

  public ProductionStrategy(BarSeries series) {
    this.series = series;
  }

  @Override
  public Strategy buildStrategy() {
    if (series == null) {
      throw new IllegalArgumentException("Series cannot be null");
    }

    closePrice = new ClosePriceIndicator(series);
    macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
    macdSignal = new EMAIndicator(macd, MACD_SIGNAL_LENGTH);
    trendEma = new EMAIndicator(closePrice, TREND_EMA);

    var buyingRule = new OverIndicatorRule(closePrice, trendEma)
        .and(new CrossedUpIndicatorRule(macd, macdSignal));

    var sellingRule = new CrossedDownIndicatorRule(macd, macdSignal)
        .or(new StopGainRule(closePrice, STOP_GAIN))
        .or(new StopLossRule(closePrice, STOP_LOSS));
    strategy = new BaseStrategy(buyingRule, sellingRule);
    return strategy;
  }

  public boolean shouldEnter() {
    return strategy.shouldEnter(series.getEndIndex());
  }

  public boolean shouldExit() {
    return strategy.shouldExit(series.getEndIndex());
  }

  public double getLastBarValue() {
    return series.getLastBar().getClosePrice().doubleValue();
  }

  @Override
  public void logStatus() {
    log.info("Current price: " + series.getLastBar().getClosePrice());
    log.info("trendEma: " + trendEma.getValue(series.getEndIndex()));
    log.info("macd: " + macd.getValue(series.getEndIndex()));
    log.info("macdSignal: " + macdSignal.getValue(series.getEndIndex()));
  }
}
