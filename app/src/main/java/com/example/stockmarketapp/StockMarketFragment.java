package com.example.stockmarketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockmarketapp.adapters.StockAdapter;
import com.example.stockmarketapp.models.Stock;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.util.Log;
import com.example.stockmarketapp.api.ApiClient;
import com.example.stockmarketapp.api.AlphaVantageService;
import com.example.stockmarketapp.models.StockResponse;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.stockmarketapp.BuildConfig;

public class StockMarketFragment extends Fragment {

    private RecyclerView stockMarketRecyclerView;
    private TextView emptyViewStockMarket;
    private List<Stock> stockMarketStocks;
    private StockAdapter stockAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_market, container, false);

        stockMarketRecyclerView = view.findViewById(R.id.stockMarketRecyclerView);
        emptyViewStockMarket = view.findViewById(R.id.emptyViewStockMarket);

        stockMarketStocks = new ArrayList<>();
        stockAdapter = new StockAdapter(stockMarketStocks);
        stockMarketRecyclerView.setAdapter(stockAdapter);
        stockMarketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchStockMarketStocks();

        return view;
    }

    private void fetchStockMarketStocks() {
        Log.d("API_CALL", "Making API call...");

        AlphaVantageService service = ApiClient.getClient().create(AlphaVantageService.class);
        Call<StockResponse> call = service.getStockData("TIME_SERIES_DAILY", "AAPL", BuildConfig.ALPHA_VANTAGE_API_KEY);

        call.enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                Log.d("API_ON_RESPONSE", "Response received");

                if (response.isSuccessful() && response.body() != null) {
                    StockResponse stockResponse = response.body();
                    StockResponse.TimeSeries timeSeries = stockResponse.getTimeSeries();
                    Map<String, StockResponse.DailyData> dailyDataMap = timeSeries.getDailyDataMap();

                    if (dailyDataMap == null || dailyDataMap.isEmpty()) {
                        emptyViewStockMarket.setText("No time series data available for the selected stock.");
                        emptyViewStockMarket.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Get the most recent date's data
                    StockResponse.DailyData dailyData = dailyDataMap.values().iterator().next();
                    if (dailyData != null) {
                        String openPrice = dailyData.getOpen();
                        Stock stock = new Stock("AAPL", Double.parseDouble(openPrice), 0.0);
                        stockMarketStocks.add(stock);
                        stockAdapter.notifyDataSetChanged();
                        emptyViewStockMarket.setVisibility(View.GONE);
                    } else {
                        emptyViewStockMarket.setText("No daily data available.");
                        emptyViewStockMarket.setVisibility(View.VISIBLE);
                    }
                } else {
                    emptyViewStockMarket.setText("Error fetching stock data from server.");
                    emptyViewStockMarket.setVisibility(View.VISIBLE);
                }
                Log.d("API_RESPONSE", new Gson().toJson(response.body()));
            }

            @Override
            public void onFailure(Call<StockResponse> call, Throwable t) {
                Log.d("API_ON_FAILURE", "API call failed");
                t.printStackTrace();
                emptyViewStockMarket.setText("Error fetching stock data from server.");
                emptyViewStockMarket.setVisibility(View.VISIBLE);
            }
        });
    }
}
