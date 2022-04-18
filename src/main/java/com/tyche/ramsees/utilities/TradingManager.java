package com.tyche.ramsees.utilities;

import com.tyche.ramsees.constants.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TradingManager {
  private Step step = Step.BUY_NEXT;

  private Double budget = 1000.0;
  private Double eth = 0.0;

  public void buy(Double buyingPrice) {
    eth = budget / buyingPrice;
    eth -= (eth * 0.001); // Binance Fees 0.1%, charged to the assets I receive
    budget = 0.0;
    setStep(Step.SELL_NEXT);

    log.info("Buying At Price: {} ", buyingPrice);
    log.info("Budget: {}", budget);
    log.info("Eth: {}", eth);
  }

  public void sell(Double sellingPrice) {
    budget = eth * sellingPrice;
    budget -= (budget * 0.001); // Binance Fees 0.1%, charged to the assets I receive
    eth = 0.0;
    setStep(Step.BUY_NEXT);

    log.info("Selling At Price: {}", sellingPrice);
    log.info("Budget: {}", budget);
    log.info("Eth: {}", eth);
  }

  public void setStep(Step step) {
    this.step = step;
  }

  public Step getStep() {
    return step;
  }

  public Double getBudget() {
    return budget;
  }

  public Double getEth() {
    return eth;
  }
}
