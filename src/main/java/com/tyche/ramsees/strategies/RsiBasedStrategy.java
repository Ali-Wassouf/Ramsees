package com.tyche.ramsees.strategies;

import org.ta4j.core.Bar;

public class RsiBasedStrategy implements RamseesBaseStrategy{

    @Override
    public void build() {

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
    public void logStatus(Bar bar, Integer endIndex) {

    }
}
