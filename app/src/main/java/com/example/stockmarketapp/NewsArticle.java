package com.example.stockmarketapp;

import com.google.gson.annotations.SerializedName;

public class NewsArticle {
    private String title;
    private String summary;
    @SerializedName("link")
    private String url;
    private String publishedDate;
    private String symbol;
    private long providerPublishTime;

    public NewsArticle(String title, String summary, String url, String publishedDate, String symbol, String link, long providerPublishTime) {
        this.title = title;
        this.summary = summary;
        this.url = url;
        this.publishedDate = publishedDate;
        this.symbol = symbol;
        this.providerPublishTime = providerPublishTime;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getSummary() { return summary; }

    public String getUrl() {
        return url;
    }

    public String getPublishedDate() { return publishedDate; }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    // Setters as needed...
}