package com.example.stockmarketapp.models;

import java.io.Serializable;

public class StockModel implements Serializable {
    private String symbol;
    private double closePrice;
    private double change;
    private double changePercent;
    private double latestPrice;

    public StockModel() {
    }

    public String getSymbol() { return symbol; }
    public double getClosePrice() { return closePrice; }
    public double getChange() { return change; }
    public double getChangePercent() { return changePercent; }
    public double getLatestPrice() { return latestPrice; }

    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setClosePrice(double closePrice) { this.closePrice = closePrice; }
    public void setChange(double change) { this.change = change; }
    public void setLatestPrice(double latestPrice) { this.latestPrice = latestPrice; }
}
