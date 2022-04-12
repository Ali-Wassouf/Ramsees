package com.tyche.ramsees.strategies;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.NumberOfPositionsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;

public class BacktestedStrategy implements RamseesBaseStrategy{
  public static final int HISTORICAL_DATA_MINIMUM_LENGTH = 100;
  public static final int HISTORICAL_DATA_LENGTH = 1000;

  private BarSeries series;
  private Strategy strategy;

  final int trendEmaLength = 100;
  final int macdShort = 12;
  final int macdLong = 26;
  final int macdSignalLength = 9;
  final Number stopGain = 0.3;
  final Number stopLoss = 1;

  ClosePriceIndicator closePrice;
  MACDIndicator macd;
  EMAIndicator macdSignal;
  EMAIndicator trendEma;

  public BacktestedStrategy(BarSeries series) {
    this.series = series;
  }

  @Override
  public Strategy buildStrategy() {
    if (series == null) {
      throw new IllegalArgumentException("Series cannot be null");
    }

    closePrice = new ClosePriceIndicator(series);
    macd = new MACDIndicator(closePrice, macdShort, macdLong);
    macdSignal = new EMAIndicator(macd, macdSignalLength);
    trendEma = new EMAIndicator(closePrice, trendEmaLength);

    var buyingRule = new OverIndicatorRule(closePrice, trendEma)
        .and(new CrossedUpIndicatorRule(macd, macdSignal));

    var sellingRule = new CrossedDownIndicatorRule(macd, macdSignal)
        .or(new StopGainRule(closePrice, stopGain))
        .or(new StopLossRule(closePrice, stopLoss));

    strategy = new BaseStrategy(buyingRule, sellingRule);
    return strategy;
  }

  @Override
  public boolean shouldEnter() {
    return false;
  }

  @Override
  public boolean shouldExit() {
    return false;
  }

  @Override
  public double getLastBarValue() {
    return 0;
  }

  @Override
  public void logStatus() {
    if(strategy == null || series == null) {
      return;
    }

    BarSeriesManager seriesManager = new BarSeriesManager(series);
    // We start by the index number TREND_EMA to have enough data to calculate EMAs
    TradingRecord tradingRecord = seriesManager.run(strategy, HISTORICAL_DATA_MINIMUM_LENGTH, HISTORICAL_DATA_LENGTH);

    /*
     * Analysis criteria
     */

    // Total profit
    GrossReturnCriterion totalReturn = new GrossReturnCriterion();
    System.out.println("Total return: " + totalReturn.calculate(series, tradingRecord));

    // Number of positions
    System.out.println("Number of positions: " + new NumberOfPositionsCriterion().calculate(series, tradingRecord));
    // Profitable position ratio
    System.out.println(
        "Winning positions ratio: " + new WinningPositionsRatioCriterion().calculate(series, tradingRecord));
    // Total profit vs buy-and-hold
    System.out.println("Custom strategy return vs buy-and-hold strategy return: "
        + new VersusBuyAndHoldCriterion(totalReturn).calculate(series, tradingRecord));

    for(Position p : tradingRecord.getPositions()){
      System.out.println(p.toString());
    }
  }
}
