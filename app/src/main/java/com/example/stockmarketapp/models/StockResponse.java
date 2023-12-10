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
        // Add other fields as per your requirement

        public String getUnderlyingSymbol() {
            return underlyingSymbol;
        }

        // Getter methods for other fields
    }

    // If there are more nested structures in the JSON, you can add more static classes here
}