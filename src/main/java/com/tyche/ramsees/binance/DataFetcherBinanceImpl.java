package com.tyche.ramsees.binance;

import com.tyche.ramsees.api.dto.PriceResponseDTO;
import com.tyche.ramsees.fetchers.DataFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DataFetcherBinanceImpl implements DataFetcher {

    private static final String BINANCE_TICKER_PRICE_ENDPOINT =
        "https://api.binance.com/api/v3/ticker/price";
    private final RestTemplate restTemplate;

    public PriceResponseDTO getPairPrice(String symbol) {
        var response =
            restTemplate.getForEntity(
                BINANCE_TICKER_PRICE_ENDPOINT + "?symbol=" + symbol,
                PriceResponseDTO.class);
        return response.getBody();
    }
}
