package com.tyche.ramsees.fetchers;


import com.tyche.ramsees.api.dto.PriceResponseDTO;

public interface DataFetcher {
  PriceResponseDTO getPairPrice(String symbol);
}
