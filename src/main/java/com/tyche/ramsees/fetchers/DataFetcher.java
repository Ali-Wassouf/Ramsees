package com.tyche.ramsees.fetchers;


import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

public interface DataFetcher {

    BarSeries getSeries();
    String getInterval();
    double getLastBarValue();
    int getEndIndex();
    Bar getLatestBar();
}
