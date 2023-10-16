//C:\Users\User\AndroidStudioProjects\StockMarketApp\app\src\main\java\com\example\stockmarketapp\StockMarketFragment.java
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
        AlphaVantageService service = ApiClient.getService();
        Call<StockResponse> call = service.getStockInfo("TIME_SERIES_DAILY", "AAPL", BuildConfig.ALPHA_VANTAGE_API_KEY);
        call.enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_RAW_RESPONSE", response.raw().toString());
                    // Convert the response body to a JSON string and log it
                    String jsonResponse = new Gson().toJson(response.body());
                    Log.d("API_RESPONSE", jsonResponse);
                    if (!response.isSuccessful()) {
                        Log.e("API_ERROR", "Response Code: " + response.code());
                        emptyViewStockMarket.setText("Error Code: " + response.code());
                        emptyViewStockMarket.setVisibility(View.VISIBLE);
                        return;
                    }

                    StockResponse.TimeSeries timeSeries = response.body().getTimeSeries();
                    Map<String, StockResponse.DailyData> dailyDataMap = timeSeries.getDailyDataMap();

                    if (dailyDataMap == null || dailyDataMap.isEmpty()) {
                        emptyViewStockMarket.setText("No time series data available for the selected stock.");
                        emptyViewStockMarket.setVisibility(View.VISIBLE);
                        return;  // Exit the method early
                    }

                    // Get the most recent date's data
                    StockResponse.DailyData dailyData = dailyDataMap.values().iterator().next();
                    if (dailyData != null) {
                        String openPrice = dailyData.getOpen();
                        // Create a new Stock object and add it to the stockMarketStocks list
                        Stock stock = new Stock("AAPL", Double.parseDouble(openPrice), 0.0); // Placeholder for change
                        stockMarketStocks.add(stock);
                        stockAdapter.notifyDataSetChanged();
                        emptyViewStockMarket.setVisibility(View.GONE);
                    } else {
                        emptyViewStockMarket.setText("No daily data available.");
                        emptyViewStockMarket.setVisibility(View.VISIBLE);
                    }
                } else {
                    emptyViewStockMarket.setText("Error fetching stock data.");
                    emptyViewStockMarket.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<StockResponse> call, Throwable t) {
                emptyViewStockMarket.setText("Failed to fetch stock data. Please check your internet connection.");
                emptyViewStockMarket.setVisibility(View.VISIBLE);
            }
        });
    }
}
