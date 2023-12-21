package com.example.stockmarketapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StockResponse {
    @SerializedName("optionChain")
    private OptionChain optionChain;

    public OptionChain getOptionChain() {
        return optionChain;
    }

    public static class OptionChain {
        @SerializedName("result")
        private List<Result> result;

        public List<Result> getResult() {
            return result;
        }
    }

    public static class Result {

        @SerializedName("quote")
        private Quote quote;

        public Quote getQuote() {
            return quote;
        }
    }

    public static class Quote {
        @SerializedName("symbol")
        private String symbol;

        @SerializedName("regularMarketPrice")
        private double regularMarketPrice;

        @SerializedName("regularMarketChangePercent")
        private double regularMarketChangePercent;

        @SerializedName("regularMarketPreviousClose")
        private double regularMarketPreviousClose;

        public String getSymbol() {
            return symbol;
        }

        public double getRegularMarketPrice() {
            return regularMarketPrice;
        }

        public double getRegularMarketChangePercent() {
            return regularMarketChangePercent;
        }

        public double getRegularMarketPreviousClose() {
            return regularMarketPreviousClose;
        }
    }
}