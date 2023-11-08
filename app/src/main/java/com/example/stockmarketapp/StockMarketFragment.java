package com.example.stockmarketapp;

import android.os.Bundle;
import android.util.Log;
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
import com.example.stockmarketapp.models.StockResponse;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Callback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=AAPL&apikey=CZX3RFB37AMFOXWL")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        emptyViewStockMarket.setText("Error fetching stock data from server.");
                        emptyViewStockMarket.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    String jsonResponse = response.body() != null ? response.body().string() : null;
                    Log.d("DIRECT_API_RESPONSE", jsonResponse);

                    Gson gson = new Gson();
                    StockResponse stockResponse = gson.fromJson(jsonResponse, StockResponse.class);

                    if (stockResponse != null && stockResponse.getTimeSeries() != null) {
                        Map<String, StockResponse.DailyData> dailyDataMap = stockResponse.getTimeSeries();

                        if (dailyDataMap != null && !dailyDataMap.isEmpty()) {
                            Log.d("API_DATA", "Data map is not empty");
                            stockMarketStocks.clear(); // Clear the list before adding new items
                            for (Map.Entry<String, StockResponse.DailyData> entry : dailyDataMap.entrySet()) {
                                StockResponse.DailyData dailyData = entry.getValue();
                                if (dailyData != null) {
                                    String date = entry.getKey();
                                    double openPrice = Double.parseDouble(dailyData.getOpen());
                                    double closePrice = Double.parseDouble(dailyData.getClose());
                                    long volume = Long.parseLong(dailyData.getVolume().replaceAll(",", ""));
                                    Stock stock = new Stock(date, openPrice, closePrice, volume);
                                    stockMarketStocks.add(stock);
                                }
                            }
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!stockMarketStocks.isEmpty()) {
                                        stockAdapter.notifyDataSetChanged();
                                        emptyViewStockMarket.setVisibility(View.GONE);
                                    } else {
                                        emptyViewStockMarket.setText("No time series data available for the selected stock.");
                                        emptyViewStockMarket.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        } else {
                            Log.d("API_DATA", "Data map is empty or null");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    emptyViewStockMarket.setText("No time series data available for the selected stock.");
                                    emptyViewStockMarket.setVisibility(View.VISIBLE);
                                });
                            }
                        }
                    } else {
                        Log.d("API_DATA", "StockResponse or TimeSeries is null");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                emptyViewStockMarket.setText("Error parsing stock data.");
                                emptyViewStockMarket.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                }
            }
        });
    }
}
