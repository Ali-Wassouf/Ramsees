package com.tyche.ramsees.strategies;

import static org.junit.jupiter.api.Assertions.*;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.NumberOfPositionsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;

@Slf4j
class MacDBasedStrategyMock extends MacDBasedStrategy {
    public static final int HISTORICAL_DATA_MINIMUM_LENGTH = 100;

    public MacDBasedStrategyMock(BarSeries series) {
        super(series);
    }

    @Override
    public void logStatus(Bar lastBae, Integer endIndex) {
        if (strategy == null) {
            return;
        }

        BarSeriesManager seriesManager = new BarSeriesManager(series);
        // We start by the index number TREND_EMA to have enough data to calculate EMAs
        TradingRecord tradingRecord = seriesManager.run(strategy, HISTORICAL_DATA_MINIMUM_LENGTH, series.getBarCount());

        /*
         * Analysis criteria
         */

        // Total profit
        GrossReturnCriterion totalReturn = new GrossReturnCriterion();
        log.info("Total return: " + totalReturn.calculate(series, tradingRecord));

        // Profitable position ratio
        log.info(("Number of positions: " + new NumberOfPositionsCriterion().calculate(series, tradingRecord)));
        // Number of positions
        log.info((
            "Winning positions ratio: " + new WinningPositionsRatioCriterion().calculate(series, tradingRecord)));
        // Total profit vs buy-and-hold
        log.info(("Custom strategy return vs buy-and-hold strategy return: "
            + new VersusBuyAndHoldCriterion(totalReturn).calculate(series, tradingRecord)));

        for (Position p : tradingRecord.getPositions()) {
            log.info(p.toString());
        }
    }

}