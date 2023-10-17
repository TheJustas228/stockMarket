// StockResponse.java
package com.example.stockmarketapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class StockResponse {
    @SerializedName("Time Series (Daily)") // This annotation is correct for the outer class
    private TimeSeries timeSeries;

    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    public static class TimeSeries {
        // Removed the redundant annotation from here
        private Map<String, DailyData> dailyDataMap;

        public Map<String, DailyData> getDailyDataMap() {
            return dailyDataMap;
        }
    }

    public static class DailyData {
        @SerializedName("1. open")
        private String open;
        @SerializedName("2. high")
        private String high;
        // Add other fields as per API response

        public String getOpen() {
            return open;
        }

        public String getHigh() {
            return high;
        }
    }
}
