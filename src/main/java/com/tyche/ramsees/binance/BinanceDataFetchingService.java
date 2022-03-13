package com.tyche.ramsees.binance;

import com.tyche.ramsees.Step;
import com.tyche.ramsees.api.dto.SlotsConfigProps;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceDataFetchingService {

    private final SlotsConfigProps slotsConfigProps;
    private final DataFetcherBinanceImpl dataFetcherBinanceImpl;

    private final Deque<Double> slowMovingAverage = new LinkedList<>();
    private final Deque<Double> fastMovingAverage = new LinkedList<>();
    private final ArrayList<Double> slowPriceHistory = new ArrayList<>();
    private final ArrayList<Double> fastPriceHistory = new ArrayList<>();
    private Step step = Step.BUY_NEXT;
    private Double buyingPrice = 0.0;
    private Double targetProfitPrice = 0.0;
    private Double stopLoss = 0.0;
    private Double budget = 1000.0;
    private Double eth = 0.0;
    private int iteration = 0;


    public void getPairPrice() {
        iteration++;
        log.info("This is iteration number {}", iteration);
        var priceResponseDTO = dataFetcherBinanceImpl.getPairPrice("ETHBUSD");
        if (slowPriceHistory.size() == slotsConfigProps.getSlow()) {
            slowPriceHistory.remove(0);
            slowMovingAverage.removeFirst();
        }
        if (fastPriceHistory.size() == slotsConfigProps.getFast()) {
            fastPriceHistory.remove(0);
            fastMovingAverage.removeFirst();
        }

        slowPriceHistory.add(Double.valueOf(priceResponseDTO.getPrice()));
        fastPriceHistory.add(Double.valueOf(priceResponseDTO.getPrice()));

        slowMovingAverage.add(calculateMovingAverage(slowPriceHistory));
        fastMovingAverage.add(calculateMovingAverage(fastPriceHistory));

        // All needed previous moving averages are saved
        if (slowPriceHistory.size() == slotsConfigProps.getSlow()) {
            log.info("Current slow moving average: {}", calculateMovingAverage(slowPriceHistory));
            log.info("Current fast moving average: {}", calculateMovingAverage(fastPriceHistory));

            if (step == Step.BUY_NEXT) {
                checkForBuyingSignal();
            } else {
                checkForSellingSignal();
            }
        }
    }

    private Double calculateMovingAverage(ArrayList<Double> priceHistory) {
        var average = 0.0;
        for (Double d : priceHistory) {
            average += d;
        }
        average /= priceHistory.size();
        return average;
    }

    private void checkForBuyingSignal() {
        if (!slowMovingAverage.isEmpty() && !fastMovingAverage.isEmpty()) {
            var currentSlowMovingAverage = slowMovingAverage.pollLast();
            var currentFastMovingAverage = fastMovingAverage.pollLast();
            var previousSlowMovingAverage = slowMovingAverage.pollLast();
            var previousFastMovingAverage = fastMovingAverage.pollLast();

            // Fast moving average crosses the slow moving average upward
            if (previousFastMovingAverage <= previousSlowMovingAverage &&
                currentFastMovingAverage > currentSlowMovingAverage) {
                buy();
            }

            // Restore data
            slowMovingAverage.addLast(previousSlowMovingAverage);
            slowMovingAverage.addLast(currentSlowMovingAverage);
            fastMovingAverage.addLast(previousFastMovingAverage);
            fastMovingAverage.addLast(currentFastMovingAverage);
        }
    }

    private void buy() {
        // Could be implemented using fastPriceHistory too
        if (!slowPriceHistory.isEmpty()) {
            buyingPrice = slowPriceHistory.get(slowPriceHistory.size() - 1);
        }
        calculateTargetProfit(buyingPrice);
        calculateStopLoss(buyingPrice);

        eth = budget / buyingPrice;
        budget = 0.0;

        step = Step.SELL_NEXT;

        log.info("Buying At Price: {} ", buyingPrice);
        log.info("Target Profit Above: {}", targetProfitPrice);
        log.info("Stop Loss At: {}", stopLoss);
        log.info("Budget: {}", budget);
        log.info("Eth: {}", eth);
    }

    private void calculateTargetProfit(final Double price) {
        Double profit = price * 0.015;
        targetProfitPrice = price + profit;
    }

    private void calculateStopLoss(final Double price) {
        var maximumLoss = price * 0.05;
        stopLoss = price - maximumLoss;
    }

    private void checkForSellingSignal() {
        // Could be implemented using fastPriceHistory too
        if (!slowPriceHistory.isEmpty()) {
            var currentPrice = slowPriceHistory.get(slowPriceHistory.size() - 1);

            if (currentPrice < stopLoss || targetProfitPrice < currentPrice) {
                sell(currentPrice);
            }
        }
    }

    private void sell(Double sellingPrice) {
        buyingPrice = eth * sellingPrice;
        eth = 0.0;

        step = Step.BUY_NEXT;
        log.info("Selling At Price: {}", sellingPrice);
        log.info("Budget: {}", budget);
        log.info("Eth: {}", eth);
    }
}
