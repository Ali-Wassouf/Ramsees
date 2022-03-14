package com.tyche.ramsees.utilities;

import java.util.ArrayList;

public class TradingCalculator {

  public Double calculateAveragePrice(ArrayList<Double> priceHistory) {
    var average = 0.0;
    for (Double d : priceHistory) {
      average += d;
    }
    average /= priceHistory.size();
    return average;
  }

  public Double calculateMinimumTargetProfit(final Double price) {
    Double profit = price * 0.0015;
    return price + profit;
  }

  public Double calculateStopLoss(final Double price) {
    var maximumLoss = price * 0.004;
    return price - maximumLoss;
  }

}
