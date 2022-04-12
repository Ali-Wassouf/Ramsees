package com.tyche.ramsees.strategies;

import org.ta4j.core.Strategy;

public interface RamseesBaseStrategy {

  Strategy buildStrategy();

  boolean shouldEnter();

  boolean shouldExit();

  double getLastBarValue();

  void logStatus();

}
