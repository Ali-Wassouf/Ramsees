package com.tyche.ramsees.binance;

import com.tyche.ramsees.Step;
import com.tyche.ramsees.api.dto.props.SlotsConfigProps;
import com.tyche.ramsees.utilities.TradingCalculator;
import com.tyche.ramsees.utilities.TradingManager;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceDataFetchingService implements ClearData {

    private final SlotsConfigProps slotsConfigProps;
    private final DataFetcherBinanceImpl dataFetcherBinanceImpl;
    private final TradingCalculator tradingCalculator = new TradingCalculator();
    private final TradingManager tradingManager = new TradingManager(this);

    private final Deque<Double> fastMovingAverage = new LinkedList<>();
    private final Deque<Double> slowMovingAverage = new LinkedList<>();
    private final ArrayList<Double> fastPriceHistory = new ArrayList<>();
    private final ArrayList<Double> slowPriceHistory = new ArrayList<>();
    private int iteration = 0;


    public void getPairPrice() {
        log.info("-----------------------------------------------");
        log.info("Iteration {}", ++iteration);

        var priceResponseDTO =
            dataFetcherBinanceImpl.getPairPrice("ETHBUSD");

        if (fastPriceHistory.size() == slotsConfigProps.getFast()) {
            fastPriceHistory.remove(0);
            fastMovingAverage.removeFirst();
        }
        if (slowPriceHistory.size() == slotsConfigProps.getSlow()) {
            slowPriceHistory.remove(0);
            slowMovingAverage.removeFirst();
        }

        fastPriceHistory.add(Double.valueOf(priceResponseDTO.getPrice()));
        slowPriceHistory.add(Double.valueOf(priceResponseDTO.getPrice()));

        fastMovingAverage.add(tradingCalculator.calculateAveragePrice(fastPriceHistory));
        slowMovingAverage.add(tradingCalculator.calculateAveragePrice(slowPriceHistory));

        // All needed previous moving averages are saved
        if (slowPriceHistory.size() == slotsConfigProps.getSlow()) {
            log.info("Current slow moving average: {}",
                tradingCalculator.calculateAveragePrice(slowPriceHistory));
            log.info("Current fast moving average: {}",
                tradingCalculator.calculateAveragePrice(fastPriceHistory));

            if (tradingManager.getStep() == Step.BUY_NEXT) {
                tradingManager.checkForBuyingSignal(
                    slowMovingAverage,
                    fastMovingAverage,
                    slowPriceHistory);
            } else {
                tradingManager.checkForSellingSignal(
                    slowMovingAverage,
                    fastMovingAverage,
                    slowPriceHistory);
            }
        }
    }

    @Override
    public void callback() {
        fastMovingAverage.clear();
        fastPriceHistory.clear();
        slowMovingAverage.clear();
        slowPriceHistory.clear();
    }
}
