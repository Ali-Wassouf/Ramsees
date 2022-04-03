package com.tyche.ramsees.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import com.google.gson.Gson;
import com.tyche.ramsees.api.dto.PriceResponseDTO;
import com.tyche.ramsees.fetchers.DataFetcher;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DataFetcherBinanceImpl implements DataFetcher {

    public PriceResponseDTO getPairPrice(String symbol) {
        SpotClientImpl client = new SpotClientImpl();

        LinkedHashMap<String,Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol","ETHBUSD");

        String result = client.createMarket().tickerSymbol(parameters);

        Gson gson = new Gson();
        PriceResponseDTO priceResponseDTO = gson.fromJson(result,
            PriceResponseDTO.class);

        return priceResponseDTO;
    }

}
