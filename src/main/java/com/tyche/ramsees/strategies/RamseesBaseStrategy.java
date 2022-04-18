package com.tyche.ramsees.strategies;

import org.ta4j.core.Bar;
import org.ta4j.core.Strategy;

public interface RamseesBaseStrategy {

    void build();

    boolean shouldEnter(int endIndex);

    boolean shouldExit(int endIndex);

    void logStatus(Bar bar, Integer endIndex);

}
