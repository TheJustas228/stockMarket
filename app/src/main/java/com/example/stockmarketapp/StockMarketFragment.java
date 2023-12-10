package com.example.stockmarketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockmarketapp.adapters.StockAdapter;
import com.example.stockmarketapp.api.ApiClient;
import com.example.stockmarketapp.api.YahooFinanceService;
import com.example.stockmarketapp.models.StockModel;
import com.example.stockmarketapp.models.StockResponse;
import java.io.IOException;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class StockMarketFragment extends Fragment implements StockAdapter.OnClickListener {

    private RecyclerView stockMarketRecyclerView;
    private List<StockModel> stockMarketStocks;
    private StockAdapter stockAdapter;
    private YahooFinanceService yahooFinanceService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_market, container, false);
        stockMarketRecyclerView = view.findViewById(R.id.stockMarketRecyclerView);
        stockMarketStocks = new ArrayList<>();
        stockAdapter = new StockAdapter(stockMarketStocks, this);
        stockMarketRecyclerView.setAdapter(stockAdapter);
        stockMarketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        yahooFinanceService = ApiClient.getClient().create(YahooFinanceService.class);
        fetchStockMarketStocks();

        return view;
    }

    private void fetchStockMarketStocks() {
        String[] stockSymbols = {"AAPL", "MSFT", "AMZN", "TSLA", "GOOGL", "GOOG", "JNJ", "UNH", "NVDA"};
        for (String symbol : stockSymbols) {
            fetchStockData(symbol);
        }
    }

    private void fetchStockData(String symbol) {
        new Thread(() -> {
            try {
                Stock yahooStock = YahooFinance.get(symbol);
                if (yahooStock != null) {
                    StockModel stockModel = new StockModel(yahooStock);
                    synchronized (stockMarketStocks) {
                        stockMarketStocks.add(stockModel);
                    }
                }
                // Update UI on the main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> stockAdapter.notifyDataSetChanged());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onStockClicked(StockModel stock) {
        // Navigate to StockDetailFragment with the selected stock
        StockDetailFragment stockDetailFragment = StockDetailFragment.newInstance(stock);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, stockDetailFragment)
                .addToBackStack(null)
                .commit();
    }
}