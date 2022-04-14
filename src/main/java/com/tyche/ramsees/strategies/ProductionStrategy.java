package com.tyche.ramsees.strategies;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.binance.BinanceDataFetcher;
import java.time.ZonedDateTime;
import java.util.List;
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

  private boolean minimumBarCountReached = false;


  public ProductionStrategy() {
    series = new BaseBarSeriesBuilder()
        .withMaxBarCount(1000)
        .withName("BINANCE_ETHBUSD")
        .build();
    closePrice = new ClosePriceIndicator(series);
    macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
    macdSignal = new EMAIndicator(macd, MACD_SIGNAL_LENGTH);
    trendEma = new EMAIndicator(closePrice, TREND_EMA);
  }

  @Override
  public Strategy buildStrategy() {
    var buyingRule = new OverIndicatorRule(closePrice, trendEma)
        .and(new CrossedUpIndicatorRule(macd, macdSignal));

    var sellingRule = new CrossedDownIndicatorRule(macd, macdSignal)
        .or(new StopGainRule(closePrice, STOP_GAIN))
        .or(new StopLossRule(closePrice, STOP_LOSS));
    strategy = new BaseStrategy(buyingRule, sellingRule);
    return strategy;
  }

  @Override
  public void fetchData(BinanceDataFetcher binanceDataFetcher) {
    List<KlineResponseDTO> klineList;
    if(!minimumBarCountReached){
      // Fetch the latest initial amount of bars in one request
      klineList = binanceDataFetcher.fetchLatestKline("ETHBUSD", getInterval(), 1000);
      minimumBarCountReached = true;
    } else {
      klineList =
          binanceDataFetcher.fetchLatestKline("ETHBUSD", getInterval(), 1);
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
  public String getInterval() {
    return "1m";
  }

  @Override
  public void logStatus() {
    log.info("Current price: " + series.getLastBar().getClosePrice());
    log.info("trendEma: " + trendEma.getValue(series.getEndIndex()));
    log.info("macd: " + macd.getValue(series.getEndIndex()));
    log.info("macdSignal: " + macdSignal.getValue(series.getEndIndex()));
  }
}
