package com.tyche.ramsees.strategies;


import com.tyche.ramsees.binance.BinanceDataFetcherMock;
import org.junit.jupiter.api.Test;

class MacDBasedStrategyTest {

    @Test
    void test() {
        var datatFetcher = new BinanceDataFetcherMock();
        var macDBased = new MacDBasedStrategyMock(datatFetcher.getSeries());
        macDBased.logStatus(datatFetcher.getLatestBar(), datatFetcher.getEndIndex());
    }
}