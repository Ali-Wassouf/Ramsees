package com.tyche.ramsees.utilities;

import com.tyche.ramsees.Step;
import com.tyche.ramsees.binance.ClearData;
import java.util.ArrayList;
import java.util.Deque;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradingManager {
  private final TradingCalculator tradingCalculator = new TradingCalculator();
  private ClearData clearData;
  private Step step = Step.BUY_NEXT;

  private Double buyingPrice = 0.0;
  private Double minimumTargetProfitPrice = 0.0;
  private Double stopLoss = 0.0;
  private Double budget = 1000.0;
  private Double eth = 0.0;

  public TradingManager(ClearData clearData) {
    this.clearData = clearData;
  }

  public void checkForBuyingSignal(
      Deque<Double> slowMovingAverage,
      Deque<Double> fastMovingAverage,
      ArrayList<Double> slowPriceHistory
      ) {
    log.info("Current budget is {}, checking for buying signal...", budget);

    if (!slowMovingAverage.isEmpty() && !fastMovingAverage.isEmpty()) {
      Double currentFastMovingAverage = fastMovingAverage.pollLast();
      Double previousFastMovingAverage = fastMovingAverage.pollLast();
      Double currentSlowMovingAverage = slowMovingAverage.pollLast();
      Double previousSlowMovingAverage = slowMovingAverage.pollLast();

      // Fast moving average crosses the slow moving average upward
      if (previousFastMovingAverage <= previousSlowMovingAverage &&
          currentFastMovingAverage > currentSlowMovingAverage) {
        log.info("Buying signal found...");
        buy(slowPriceHistory); // Could use fastPriceHistory too
      }

      // Restore data
      slowMovingAverage.addLast(previousSlowMovingAverage);
      slowMovingAverage.addLast(currentSlowMovingAverage);
      fastMovingAverage.addLast(previousFastMovingAverage);
      fastMovingAverage.addLast(currentFastMovingAverage);
    }
  }

  private void buy(ArrayList<Double> slowPriceHistory) {
    // Could be implemented using fastPriceHistory too
    if (!slowPriceHistory.isEmpty()) {
      buyingPrice = slowPriceHistory.get(slowPriceHistory.size() - 1);
    }
    minimumTargetProfitPrice = tradingCalculator.calculateMinimumTargetProfit(buyingPrice);
    stopLoss = tradingCalculator.calculateStopLoss(buyingPrice);

    eth = budget / buyingPrice;
    eth -= (eth * 0.001); // Binance Fees 0.1%, charged to the assets I receive
    budget = 0.0;

    step = Step.SELL_NEXT;

    log.info("Buying At Price: {} ", buyingPrice);
    log.info("Minimum target Profit Above: {}", minimumTargetProfitPrice);
    log.info("Stop Loss At: {}", stopLoss);
    log.info("Budget: {}", budget);
    log.info("Eth: {}", eth);
  }

  public void checkForSellingSignal(
      Deque<Double> slowMovingAverage,
      Deque<Double> fastMovingAverage,
      ArrayList<Double> slowPriceHistory
      ) {
    log.info("Current Eth is {}, checking for selling signal...", eth);

    if (!slowMovingAverage.isEmpty() && !fastMovingAverage.isEmpty()) {
      Double currentSlowMovingAverage = slowMovingAverage.pollLast();
      Double currentFastMovingAverage = fastMovingAverage.pollLast();
      Double previousSlowMovingAverage = slowMovingAverage.pollLast();
      Double previousFastMovingAverage = fastMovingAverage.pollLast();

      Double currentPrice = slowPriceHistory.get(slowPriceHistory.size() - 1);
      // Fast moving average crosses the slow moving average upward
      if (currentPrice < stopLoss ||
          (previousFastMovingAverage >= previousSlowMovingAverage &&
          currentFastMovingAverage < currentSlowMovingAverage &&
          minimumTargetProfitPrice < currentPrice)
      ) {
            log.info("Selling signal found...");
            sell(currentPrice); // Could use fastPriceHistory too
      }

      // Restore data
      slowMovingAverage.addLast(previousSlowMovingAverage);
      slowMovingAverage.addLast(currentSlowMovingAverage);
      fastMovingAverage.addLast(previousFastMovingAverage);
      fastMovingAverage.addLast(currentFastMovingAverage);
    }
  }

  public void sell(Double sellingPrice) {
    budget = eth * sellingPrice;
    budget -= (budget * 0.001); // Binance Fees 0.1%, charged to the assets I receive
    eth = 0.0;

    step = Step.BUY_NEXT;
    log.info("Selling At Price: {}", sellingPrice);
    log.info("Budget: {}", budget);
    log.info("Eth: {}", eth);

    /*
    * Since the price update interval is too short, it's likely for the fast moving average to cross
    * the slow one upward again at the top after selling just before prices goes down at a start of
    * a downward trend. A simple fix is to clear the data and collect it again which will prevent
    * the buying for some time.
    */
    clearData.callback();
  }

  public Step getStep() {
    return step;
  }
}
