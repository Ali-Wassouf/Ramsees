package com.tyche.ramsees.strategies;

import com.tyche.ramsees.binance.BinanceDataFetcher;
import org.ta4j.core.Strategy;

public interface RamseesBaseStrategy {

  Strategy buildStrategy();

  void fetchData(BinanceDataFetcher binanceDataFetcher);

  boolean shouldEnter();

  boolean shouldExit();

  double getLastBarValue();

  String getInterval();

  void logStatus();

}
