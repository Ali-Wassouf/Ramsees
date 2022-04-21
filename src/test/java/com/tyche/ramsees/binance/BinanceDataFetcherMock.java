package com.tyche.ramsees.binance;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import java.time.ZonedDateTime;
import org.ta4j.core.BarSeries;

public class BinanceDataFetcherMock extends BinanceDataFetcher {

    @Override
    public BarSeries getSeries() {
        var halfDayMills = 1000 * 60 * 60 * 12;
        var endTime = Long.parseLong(this.getServerTime());
        var startTime = endTime - halfDayMills;
        var halfDays = 28 * 2;

        for (int i = 0; i < halfDays; i++) {
            var klineList =
                fetchLatestKline(
                    "ETHBUSD",
                    getInterval(),
                    startTime,
                    endTime,
                    1000);

            for (KlineResponseDTO k : klineList) {
                series.addBar(
                    ZonedDateTime.now(),
                    Double.valueOf(k.getOpen()),
                    Double.valueOf(k.getHigh()),
                    Double.valueOf(k.getLow()),
                    Double.valueOf(k.getClose()),
                    Double.valueOf(k.getVolume())
                );
            }

            endTime -= halfDayMills;
            startTime -= halfDayMills;
        }
        return series;
    }
}
