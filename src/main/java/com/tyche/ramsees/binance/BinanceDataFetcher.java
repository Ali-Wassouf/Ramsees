package com.tyche.ramsees.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.api.dto.PriceResponseDTO;
import com.tyche.ramsees.api.dto.ServerTimeResponseDTO;
import com.tyche.ramsees.fetchers.DataFetcher;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceDataFetcher implements DataFetcher {

    private final BarSeries series;
    private boolean minimumBarCountReached = false;

    public String getServerTime() {
        var client = new SpotClientImpl();
        var result = client.createMarket().time();

        var gson = new Gson();
        return gson.fromJson(result,
            ServerTimeResponseDTO.class).getServerTime();
    }

    @Override
    public String getInterval() {
        return "5m";
    }

    @Override
    public BarSeries getSeries() {
        List<KlineResponseDTO> klineList;
        if (!minimumBarCountReached) {
            // Fetch the latest initial amount of bars in one request
            klineList = this.fetchLatestKline("ETHBUSD", getInterval(), 1000);
            minimumBarCountReached = true;
        } else {
            klineList =
                this.fetchLatestKline("ETHBUSD", getInterval(), 1);
        }

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
        return series;
    }

    @Override
    public double getLastBarValue() {
        return series.getLastBar().getClosePrice().doubleValue();
    }

    @Override
    public Bar getLatestBar(){
        return series.getLastBar();
    }

    @Override
    public int getEndIndex() {
        return series.getEndIndex();
    }

    private List<KlineResponseDTO> fetchLatestKline(String symbol, String interval, Integer limit) {
        return fetchLatestKline(symbol, interval, null, null, limit);
    }

    private List<KlineResponseDTO> fetchLatestKline(
        String symbol,
        String interval,
        Long startTime,
        Long endTime,
        Integer limit) {
        var client = new SpotClientImpl();

        var parameters = new LinkedHashMap<String, Object>();
        parameters.put("symbol", symbol);
        parameters.put("interval", interval);
        parameters.put("limit", limit);

        if (startTime != null) {
            parameters.put("startTime", startTime);
        }

        if (endTime != null) {
            parameters.put("endTime", endTime);
        }

        var result = client.createMarket().klines(parameters);

        var jsonArray = new JSONArray(result);
        var klineList = new ArrayList<KlineResponseDTO>();

        for (Object o : jsonArray) {
            try {
                var klineResponseDTO =
                    new ObjectMapper().readValue(o.toString(), KlineResponseDTO.class);
                klineList.add(klineResponseDTO);
            } catch (JsonProcessingException e) {
                log.info("Exception while fetching the klines", e);
            }
        }

        return klineList;
    }
}
