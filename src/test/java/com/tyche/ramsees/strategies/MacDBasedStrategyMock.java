package com.tyche.ramsees.strategies;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.NumberOfPositionsCriterion;
import org.ta4j.core.analysis.criteria.NumberOfWinningPositionsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.analysis.criteria.pnl.NetProfitCriterion;
import org.ta4j.core.cost.LinearTransactionCostModel;
import org.ta4j.core.cost.ZeroCostModel;

@Slf4j
class MacDBasedStrategyMock extends MacDBasedStrategy {
    public static final int HISTORICAL_DATA_MINIMUM_LENGTH = 100;

    public MacDBasedStrategyMock(BarSeries series) {
        super(series);
    }

    @Override
    public void logStatus(Bar lastBar, Integer endIndex) {
        if (strategy == null) {
            return;
        }

        BarSeriesManager seriesManager = new BarSeriesManager(series, new LinearTransactionCostModel(0.001), new ZeroCostModel());
        TradingRecord tradingRecord = seriesManager.run(strategy, HISTORICAL_DATA_MINIMUM_LENGTH, series.getBarCount());

        /*
         * Analysis criteria
         */

        // Profit
        GrossReturnCriterion totalReturn = new GrossReturnCriterion();
        log.info("Total return: " + totalReturn.calculate(series, tradingRecord));
        NetProfitCriterion netProfitCriterion = new NetProfitCriterion();
        log.info("Net profit: " + netProfitCriterion.calculate(series, tradingRecord));

        // Profitable position ratio
        log.info(("Number of positions: " + new NumberOfPositionsCriterion().calculate(series, tradingRecord)));
        // Number of positions
        log.info((
            "Number of winning positions: " + new NumberOfWinningPositionsCriterion().calculate(series, tradingRecord)));
        // Total profit vs buy-and-hold
        log.info(("Custom strategy return vs buy-and-hold strategy return: "
            + new VersusBuyAndHoldCriterion(totalReturn).calculate(series, tradingRecord)));

        for (Position p : tradingRecord.getPositions()) {
            log.info(p.toString());
        }
    }

}