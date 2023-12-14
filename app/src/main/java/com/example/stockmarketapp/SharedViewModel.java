package com.example.stockmarketapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stockmarketapp.models.StockModel;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<List<StockModel>> trackedStocks = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<StockModel>> getTrackedStocks() {
        return trackedStocks;
    }

    public void addStock(StockModel stock) {
        List<StockModel> currentStocks = trackedStocks.getValue();
        if (currentStocks == null) {
            currentStocks = new ArrayList<>();
        }

        boolean stockExists = false;
        for (StockModel existingStock : currentStocks) {
            if (existingStock.getSymbol().equalsIgnoreCase(stock.getSymbol())) {
                stockExists = true;
                break;
            }
        }

        if (!stockExists) {
            currentStocks.add(stock);
            trackedStocks.postValue(currentStocks);
        }
    }

}