package com.tyche.ramsees.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Kline/Candlestick bars for a symbol. Klines are uniquely identified by their open time.
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlineResponseDTO {
    private Long openTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private Long closeTime;
    private String quoteAssetVolume;
    private Long numberOfTrades;
    private String takerBuyBaseAssetVolume;
    private String takerBuyQuoteAssetVolume;
    private String unknown;

    @Override
    public String toString() {
        return "openTime: " + openTime
            + "open: " + open
            + "high: " + high
            + "low: " + low
            + "close: " + close
            + "volume: " + volume
            + "closeTime: " + closeTime
            + "quoteAssetVolume: " + quoteAssetVolume
            + "numberOfTrades: " + numberOfTrades
            + "takerBuyBaseAssetVolume: " + takerBuyBaseAssetVolume
            + "takerBuyQuoteAssetVolume: " + takerBuyQuoteAssetVolume;
    }
}
