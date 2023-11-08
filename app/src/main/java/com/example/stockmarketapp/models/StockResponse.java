package com.example.stockmarketapp.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class StockResponse {
    @SerializedName("Time Series (Daily)")
    private Map<String, DailyData> timeSeries;

    public Map<String, DailyData> getTimeSeries() {
        return timeSeries;
    }

    @Override
    public String toString() {
        // Convert the object to its JSON representation using Gson
        return new Gson().toJson(this);
    }

    public static class TimeSeries {
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
        @SerializedName("4. close")
        private String close;
        @SerializedName("5. volume")
        private String volume;
        @SerializedName("6. low")
        private String low;

        public String getClose() { return close; }
        public String getVolume() { return volume; }
        public String getOpen() {
            return open;
        }
        public String getHigh() {
            return high;
        }
        public String getLow() { return low; }
    }
}