package com.example.stockmarketapp.models;

import java.io.Serializable;

public class Stock implements Serializable {
    private String name; // This will hold the stock symbol
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private double change;
    private long volume;

    public Stock(String name, double openPrice, double highPrice, double lowPrice, double closePrice, double change, long volume) {
        this.name = name;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.change = closePrice - openPrice;
        this.volume = volume;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getChange() {
        return change;
    }

    public long getVolume() {
        return volume;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }
}