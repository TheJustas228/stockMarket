package com.example.stockmarketapp;

import android.os.Bundle;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockMarketFragment extends Fragment {

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

        // Updated constructor call to match the StockAdapter definition
        stockAdapter = new StockAdapter(getContext(),
                stockMarketStocks,
                this::onStockSelected,
                stock -> {
                    // Empty implementation for remove functionality
                }, false
        );

        stockMarketRecyclerView.setAdapter(stockAdapter);
        stockMarketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        yahooFinanceService = ApiClient.getClient().create(YahooFinanceService.class);
        fetchStockMarketStocks();

        return view;
    }

    private void onStockSelected(StockModel stock) {
        StockGraphFragment stockGraphFragment = StockGraphFragment.newInstance(stock);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, stockGraphFragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchStockMarketStocks() {
        String[] stockSymbols = {"aapl", "msft", "amzn", "tsla", "googl", "goog", "jnj", "unh", "nvda"};
        for (String symbol : stockSymbols) {
            fetchStockData(symbol);
        }
    }

    private void fetchStockData(String symbol) {
        Call<StockResponse> call = yahooFinanceService.getStockOptions(symbol);
        call.enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StockResponse stockResponse = response.body();
                    StockResponse.Quote quote = stockResponse.getOptionChain().getResult().get(0).getQuote();
                    StockModel stock = new StockModel();
                    stock.setSymbol(quote.getSymbol());
                    stock.setClosePrice(quote.getRegularMarketPrice());
                    stock.setChange(quote.getRegularMarketChange());
                    stockMarketStocks.add(stock);
                    stockAdapter.notifyDataSetChanged();
                } else {
                    Log.e("StockMarketFragment", "Response not successful for symbol: " + symbol);
                }
            }

            @Override
            public void onFailure(Call<StockResponse> call, Throwable t) {
                Log.e("StockMarketFragment", "Error fetching stock data for " + symbol, t);
            }
        });
    }

    // If you still want to handle stock clicks, define the method here
    // and update your adapter to handle click events.
}