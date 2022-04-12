package com.tyche.ramsees.binance;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.strategies.RamseesBaseStrategy;
import com.tyche.ramsees.strategies.ProductionStrategy;
import java.time.ZonedDateTime;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceDataMonitor {
    private final BinanceDataFetcher binanceDataFetcher;
    private final BarSeries series = new BaseBarSeriesBuilder()
        .withMaxBarCount(1000)
        .withName("BINANCE_ETHBUSD")
        .build();
    private final RamseesBaseStrategy strategy = new ProductionStrategy(series);

    private int iteration = 0;
    private boolean minimumBarCountReached = false;

    @PostConstruct
    public void init() {
        strategy.buildStrategy();
    }

    public void updateKlines() {
        log.info("-----------------------------------------------");
        log.info("Iteration {}", ++iteration);

        List<KlineResponseDTO> klineList;
        if(!minimumBarCountReached){
            // Fetch the latest initial amount of bars in one request
            klineList =
                binanceDataFetcher.fetchLatestKline("ETHBUSD", "1m", 1000);
            minimumBarCountReached = true;
        } else {
            klineList =
                binanceDataFetcher.fetchLatestKline("ETHBUSD", "1m", 1);
        }


        for(KlineResponseDTO k : klineList) {
            series.addBar(
                ZonedDateTime.now(),
                Double.valueOf(k.getOpen()),
                Double.valueOf(k.getHigh()),
                Double.valueOf(k.getLow()),
                Double.valueOf(k.getClose()),
                Double.valueOf(k.getVolume())
            );
        }

        strategy.logStatus();
    }

    public boolean shouldEnter() {
        return strategy.shouldEnter();
    }

    public boolean shouldExit() {
        return strategy.shouldExit();
    }

    public double getLastBarValue() {
        return strategy.getLastBarValue();
    }
}
