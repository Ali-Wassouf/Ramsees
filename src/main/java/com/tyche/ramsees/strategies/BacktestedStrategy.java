package com.tyche.ramsees.strategies;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.binance.BinanceDataFetcher;
import java.time.ZonedDateTime;
import org.ta4j.core.Bar;
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
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class BacktestedStrategy implements RamseesBaseStrategy{
  public static final int HISTORICAL_DATA_MINIMUM_LENGTH = 100;
  public static final long THREE_MONTHS_SHIFT = 8035200000L;
  public static final long SIX_MONTHS_SHIFT = 16070400000L;
  public static final long NINE_MONTHS_SHIFT = 24105600000L;

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
        .withName("BINANCE_ETHBUSD")
        .build();
    closePrice = new ClosePriceIndicator(series);
    macd = new MACDIndicator(closePrice, macdShort, macdLong);
    macdSignal = new EMAIndicator(macd, macdSignalLength);
    trendEma = new EMAIndicator(closePrice, trendEmaLength);
  }

  @Override
  public Strategy build() {
    var buyingRule = new CrossedUpIndicatorRule(macd, macdSignal)
        .and((i, tradingRecord) -> (macd.getValue(i).doubleValue() < 0))
        .and(new UnderIndicatorRule(closePrice, trendEma));

    var sellingRule = new StopGainRule(closePrice, stopGain)
        .or(new StopLossRule(closePrice, stopLoss));

    strategy = new BaseStrategy(buyingRule, sellingRule);
    return strategy;
  }

  @Override
  public void fetchData(BinanceDataFetcher binanceDataFetcher) {
    var halfDayMills = 1000 * 60 * 60 * 12;
    var endTime = Long.valueOf(binanceDataFetcher.getServerTime()) - NINE_MONTHS_SHIFT;
    var startTime = endTime - halfDayMills;
    var halfDays = 28 * 2;

    for(int i = 0; i < halfDays ; i++){
      var klineList =
          binanceDataFetcher.fetchLatestKline(
              "ETHBUSD",
              getInterval(),
              startTime,
              endTime,
              1000);

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

      endTime -= halfDayMills;
      startTime -= halfDayMills;
    }
  }

  @Override
  public boolean shouldEnter(int endIndex) {
    return false;
  }

  @Override
  public boolean shouldExit(int endIndex) {
    return false;
  }

  @Override
  public double getLastBarValue() {
    return 0;
  }

  public String getInterval() {
    return "5m";
  }

  @Override
  public void logStatus(Bar lastBae, int endIndex) {
    if(strategy == null || series == null) {
      return;
    }

    BarSeriesManager seriesManager = new BarSeriesManager(series);
    // We start by the index number TREND_EMA to have enough data to calculate EMAs
    TradingRecord tradingRecord = seriesManager.run(strategy, HISTORICAL_DATA_MINIMUM_LENGTH, series.getBarCount());

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
