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

    // Constructor to create StockModel from yahoofinance.Stock object
    public StockModel(Stock stock) {
        StockQuote quote = stock.getQuote();

        this.symbol = stock.getSymbol();
        this.openPrice = quote.getOpen().doubleValue();
        this.highPrice = quote.getDayHigh().doubleValue();
        this.lowPrice = quote.getDayLow().doubleValue();
        this.closePrice = quote.getPrice().doubleValue();
        this.change = quote.getChange().doubleValue();
        this.changePercent = quote.getChangeInPercent().doubleValue();
        this.volume = quote.getVolume();
    }

    // Getters
    public String getSymbol() { return symbol; }
    public double getOpenPrice() { return openPrice; }
    public double getHighPrice() { return highPrice; }
    public double getLowPrice() { return lowPrice; }
    public double getClosePrice() { return closePrice; }
    public double getChange() { return change; }
    public double getChangePercent() { return changePercent; }
    public long getVolume() { return volume; }
}