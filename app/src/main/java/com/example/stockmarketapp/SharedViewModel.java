package com.example.stockmarketapp;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.stockmarketapp.models.StockModel;
import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<List<StockModel>> trackedStocks = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<StockModel>> getTrackedStocks() {
        return trackedStocks;
    }

    public void setTrackedStocks(List<StockModel> stockList) {
        trackedStocks.setValue(stockList);
    }

    public void trackStock(StockModel stock) {
        List<StockModel> currentStocks = trackedStocks.getValue();
        if (currentStocks == null) {
            currentStocks = new ArrayList<>();
        }
        if (!currentStocks.contains(stock)) {
            currentStocks.add(stock);
            trackedStocks.setValue(currentStocks); // Immediate update
            Log.d("SharedViewModel", "Stock added: " + stock.getSymbol());
        } else {
            Log.d("SharedViewModel", "Stock already tracked: " + stock.getSymbol());
        }
    }

    public boolean isStockTracked(String symbol) {
        List<StockModel> currentStocks = trackedStocks.getValue();
        if (currentStocks != null) {
            for (StockModel stock : currentStocks) {
                if (stock.getSymbol().equalsIgnoreCase(symbol)) {
                    return true; // Stock is already tracked
                }
            }
        }
        return false; // Stock is not tracked
    }

    public void removeStock(StockModel stock) {
        List<StockModel> currentStocks = new ArrayList<>(trackedStocks.getValue());
        if (currentStocks.contains(stock)) {
            currentStocks.remove(stock);
            trackedStocks.postValue(currentStocks);
        }
    }
}