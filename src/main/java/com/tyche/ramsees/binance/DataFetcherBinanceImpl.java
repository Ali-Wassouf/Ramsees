package com.tyche.ramsees.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.api.dto.PriceResponseDTO;
import com.tyche.ramsees.fetchers.DataFetcher;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataFetcherBinanceImpl implements DataFetcher {

    public PriceResponseDTO getPairPrice(String symbol) {
        SpotClientImpl client = new SpotClientImpl();

        LinkedHashMap<String,Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);

        String result = client.createMarket().tickerSymbol(parameters);

        Gson gson = new Gson();
        PriceResponseDTO priceResponseDTO = gson.fromJson(result,
            PriceResponseDTO.class);

        return priceResponseDTO;
    }

    public ArrayList<KlineResponseDTO> fetchLatestKline(String symbol, String interval) {
        SpotClientImpl client = new SpotClientImpl();

        LinkedHashMap<String,Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("interval", interval);
        parameters.put("limit", 1);

        String result = client.createMarket().klines(parameters);

        JSONArray jsonArray = new JSONArray(result);
        ArrayList<KlineResponseDTO> klineList = new ArrayList<>();

        for(Object o : jsonArray){
            try {
                KlineResponseDTO klineResponseDTO =
                    new ObjectMapper().readValue(o.toString(), KlineResponseDTO.class);
                klineList.add(klineResponseDTO);
            } catch (JsonProcessingException e) {
                log.info(e.getMessage());
            }
        }

        return klineList;
    }
}
