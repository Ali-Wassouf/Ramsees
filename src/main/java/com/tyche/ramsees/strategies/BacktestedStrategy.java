package com.tyche.ramsees.strategies;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.binance.BinanceDataFetcher;
import java.time.ZonedDateTime;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseBarSeriesBuilder;
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
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
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
  final Number stopGain = 0.5;
  final Number stopLoss = 0.3;

  ClosePriceIndicator closePrice;
  MACDIndicator macd;
  EMAIndicator macdSignal;
  EMAIndicator trendEma;

  public BacktestedStrategy() {
    series = new BaseBarSeriesBuilder()
        .withMaxBarCount(1000)
        .withName("BINANCE_ETHBUSD")
        .build();
    closePrice = new ClosePriceIndicator(series);
    macd = new MACDIndicator(closePrice, macdShort, macdLong);
    macdSignal = new EMAIndicator(macd, macdSignalLength);
    trendEma = new EMAIndicator(closePrice, trendEmaLength);
  }

  @Override
  public Strategy buildStrategy() {
    var buyingRule = new CrossedUpIndicatorRule(macd, macdSignal)
        .and((i, tradingRecord) -> (macd.getValue(i).doubleValue() < 0))
        .and(new OverIndicatorRule(closePrice, trendEma));

    var sellingRule = new StopGainRule(closePrice, stopGain)
        .or(new StopLossRule(closePrice, stopLoss));

    strategy = new BaseStrategy(buyingRule, sellingRule);
    return strategy;
  }

  @Override
  public void fetchData(BinanceDataFetcher binanceDataFetcher) {
    var klineList =
        binanceDataFetcher.fetchLatestKline("ETHBUSD", getInterval(), 1000);

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
  public String getInterval() {
    return "3m";
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
