package com.example.stockmarketapp.models;

import java.io.Serializable;
import yahoofinance.Stock;
import yahoofinance.quotes.stock.StockQuote;

public class StockModel implements Serializable {
    private String symbol; // Stock symbol
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private double change;
    private double changePercent;
    private long volume;

    // No-argument constructor
    public StockModel() {
        // Initialize fields with default values or leave them uninitialized
    }

    // Constructor to create StockModel from yahoofinance.Stock object
    public StockModel(Stock stock) {
        StockQuote quote = stock.getQuote();

        this.symbol = stock.getSymbol();
        this.openPrice = quote.getOpen() != null ? quote.getOpen().doubleValue() : 0.0;
        this.highPrice = quote.getDayHigh() != null ? quote.getDayHigh().doubleValue() : 0.0;
        this.lowPrice = quote.getDayLow() != null ? quote.getDayLow().doubleValue() : 0.0;
        this.closePrice = quote.getPrice() != null ? quote.getPrice().doubleValue() : 0.0;
        this.change = quote.getChange() != null ? quote.getChange().doubleValue() : 0.0;
        this.changePercent = quote.getChangeInPercent() != null ? quote.getChangeInPercent().doubleValue() : 0.0;
        this.volume = quote.getVolume() != null ? quote.getVolume() : 0;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public double getOpenPrice() { return openPrice; }
    public double getHighPrice() { return highPrice; }
    public double getLowPrice() { return lowPrice; }
    public double getClosePrice() { return closePrice; }
    public double getChange() { return change; }
    public double getChangePercent() { return changePercent; }
    public long getVolume() { return volume; }

    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setOpenPrice(double openPrice) { this.openPrice = openPrice; }
    public void setHighPrice(double highPrice) { this.highPrice = highPrice; }
    public void setLowPrice(double lowPrice) { this.lowPrice = lowPrice; }
    public void setClosePrice(double closePrice) { this.closePrice = closePrice; }
    public void setChange(double change) { this.change = change; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }
    public void setVolume(long volume) { this.volume = volume; }
}
