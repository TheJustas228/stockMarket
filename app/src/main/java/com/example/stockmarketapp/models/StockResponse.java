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
        @SerializedName("underlyingSymbol")
        private String underlyingSymbol;

        @SerializedName("quote")
        private Quote quote;

        public String getUnderlyingSymbol() {
            return underlyingSymbol;
        }

        public Quote getQuote() {
            return quote;
        }
    }

    public static class Quote {
        @SerializedName("symbol")
        private String symbol;

        @SerializedName("regularMarketPrice")
        private double regularMarketPrice;

        @SerializedName("regularMarketChange")
        private double regularMarketChange;

        @SerializedName("regularMarketChangePercent")
        private double regularMarketChangePercent;

        // Additional fields can be added here based on your requirements

        public String getSymbol() {
            return symbol;
        }

        public double getRegularMarketPrice() {
            return regularMarketPrice;
        }

        public double getRegularMarketChange() {
            return regularMarketChange;
        }

        public double getRegularMarketChangePercent() {
            return regularMarketChangePercent;
        }

        // Getter methods for additional fields
    }

    // Additional nested classes can be added here if there are more structures in the JSON
}