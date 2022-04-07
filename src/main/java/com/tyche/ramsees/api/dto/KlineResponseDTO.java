package com.tyche.ramsees.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import lombok.Data;

/**
 * Kline/Candlestick bars for a symbol. Klines are uniquely identified by their open time.
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({
    "openTime",
    "open",
    "high",
    "low",
    "close",
    "volume",
    "closeTime",
    "quoteAssetVolume",
    "numberOfTrades",
    "takerBuyBaseAssetVolume",
    "takerBuyQuoteAssetVolume",
    "unknown"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlineResponseDTO {
    public Long openTime;
    public String open;
    public String high;
    public String low;
    public String close;
    public String volume;
    public Long closeTime;
    public String quoteAssetVolume;
    public Long numberOfTrades;
    public String takerBuyBaseAssetVolume;
    public String takerBuyQuoteAssetVolume;
    public String unknown;

    @JsonCreator
    public KlineResponseDTO(
        @JsonProperty("openTime")
        Long openTime,
        @JsonProperty("open")
        String open,
        @JsonProperty("high")
        String high,
        @JsonProperty("low")
        String low,
        @JsonProperty("close")
        String close,
        @JsonProperty("volume")
        String volume,
        @JsonProperty("closeTime")
        Long closeTime,
        @JsonProperty("quoteAssetVolume")
        String quoteAssetVolume,
        @JsonProperty("numberOfTrades")
        Long numberOfTrades,
        @JsonProperty("takerBuyBaseAssetVolume")
        String takerBuyBaseAssetVolume,
        @JsonProperty("takerBuyQuoteAssetVolume")
        String takerBuyQuoteAssetVolume,
        @JsonProperty("unknown")
        String unknown
    ){
        this.openTime = openTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.closeTime = closeTime;
        this.quoteAssetVolume = quoteAssetVolume;
        this.numberOfTrades = numberOfTrades;
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
        this.unknown = unknown;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("openTime: ").append(openTime)
            .append("open: ").append(open)
            .append("high: ").append(high)
            .append("low: ").append(low)
            .append("close: ").append(close)
            .append("volume: ").append(volume)
            .append("closeTime: ").append(closeTime)
            .append("quoteAssetVolume: ").append(quoteAssetVolume)
            .append("numberOfTrades: ").append(numberOfTrades)
            .append("takerBuyBaseAssetVolume: ").append(takerBuyBaseAssetVolume)
            .append("takerBuyQuoteAssetVolume: ").append(takerBuyQuoteAssetVolume)
            .toString();
    }
}
