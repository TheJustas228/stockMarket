package com.example.stockmarketapp;

import com.google.gson.annotations.SerializedName;

public class NewsArticle {
    private String title;
    @SerializedName("link")
    private String url;
    private String symbol;

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}