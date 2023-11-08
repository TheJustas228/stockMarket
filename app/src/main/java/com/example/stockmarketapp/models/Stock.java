package com.example.stockmarketapp.models;

public class Stock {
    private String name; // This will hold the date for display
    private double price;
    private double change;
    private long volume;

    public Stock(String date, double openPrice, double closePrice, long volume) {
        this.name = date;
        this.price = openPrice;
        this.change = closePrice - openPrice;
        this.volume = volume;
    }

    // Getters
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getChange() { return change; }
    public long getVolume() { return volume; }
}