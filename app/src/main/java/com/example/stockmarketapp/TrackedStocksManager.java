package com.example.stockmarketapp;

import com.example.stockmarketapp.models.StockModel;

import java.util.ArrayList;
import java.util.List;

public class TrackedStocksManager {
    private static final TrackedStocksManager ourInstance = new TrackedStocksManager();
    private final List<StockModel> trackedStocks = new ArrayList<>();

    public static TrackedStocksManager getInstance() {
        return ourInstance;
    }

    private TrackedStocksManager() {
    }

    public List<StockModel> getTrackedStocks() {
        return trackedStocks;
    }

    public void addStock(StockModel stock) {
        if (!trackedStocks.contains(stock)) {
            trackedStocks.add(stock);
        }
    }
}
