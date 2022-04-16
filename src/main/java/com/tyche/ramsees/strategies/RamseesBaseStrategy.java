package com.tyche.ramsees.strategies;

import com.tyche.ramsees.binance.BinanceDataFetcher;
import org.ta4j.core.Bar;
import org.ta4j.core.Strategy;

public interface RamseesBaseStrategy {

  Strategy build();

  void fetchData(BinanceDataFetcher binanceDataFetcher);

  boolean shouldEnter(int endIndex);

  boolean shouldExit(int endIndex);

  double getLastBarValue();

  void logStatus(Bar lastBar, int endIndex);

}
