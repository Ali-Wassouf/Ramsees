package com.tyche.ramsees.binance;

import com.tyche.ramsees.api.dto.KlineResponseDTO;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Stack;
import org.ta4j.core.BarSeries;

public class BinanceDataFetcherMock extends BinanceDataFetcher {

    @Override
    public BarSeries getSeries() {
        var halfDayMills = 1000 * 60 * 60 * 12;
        var endTime = Long.parseLong(this.getServerTime());
        var startTime = endTime - halfDayMills;
        var halfDays = 28 * 2;

        // Used to fix the order of bars we push in the series
        Stack<KlineResponseDTO> stack = new Stack<KlineResponseDTO>();

        for (int i = 0; i < halfDays; i++) {
            var klineList =
                fetchLatestKline(
                    "ETHBUSD",
                    getInterval(),
                    startTime,
                    endTime,
                    1000);

            for(int j = klineList.size() - 1; j >= 0 ; j--){
                stack.push(klineList.get(j));
            }

            endTime -= halfDayMills;
            startTime -= halfDayMills;
        }

        while (!stack.isEmpty()) {
            var k = stack.pop();
            series.addBar(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(k.getCloseTime()), ZoneId.systemDefault()),
                Double.valueOf(k.getOpen()),
                Double.valueOf(k.getHigh()),
                Double.valueOf(k.getLow()),
                Double.valueOf(k.getClose()),
                Double.valueOf(k.getVolume())
            );
        }

        return series;
    }

    public ZonedDateTime millsToLocalDateTime(long m){
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = Instant.ofEpochSecond(m);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime;
    }
}
