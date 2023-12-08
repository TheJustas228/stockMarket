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
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private RecyclerView trackedStocksRecyclerView;
    private TextView emptyView;
    private List<Stock> trackedStocks;
    private StockAdapter stockAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        trackedStocksRecyclerView = view.findViewById(R.id.trackedStocksRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        trackedStocks = new ArrayList<>();
        stockAdapter = new StockAdapter(trackedStocks, stock -> {
            // Handle stock click here
        });
        trackedStocksRecyclerView.setAdapter(stockAdapter);
        trackedStocksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchTrackedStocks();

        return view;
    }

    private void fetchTrackedStocks() {
        // Top 10 stocks from the S&P 500 list (example symbols)
        String[] stockSymbols = {"AAPL", "MSFT", "AMZN", "FB", "GOOGL", "JNJ", "V", "PG", "JPM", "UNH"};

        for (String symbol : stockSymbols) {
            fetchStockData(symbol);
        }
    }

    private void fetchStockData(String symbol) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=YOUR_API_KEY";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        emptyView.setText("Error fetching stock data from server.");
                        emptyView.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    String jsonResponse = response.body() != null ? response.body().string() : null;
                    Gson gson = new Gson();
                    StockResponse stockResponse = gson.fromJson(jsonResponse, StockResponse.class);

                    if (stockResponse != null && stockResponse.getTimeSeries() != null) {
                        Map<String, StockResponse.DailyData> dailyDataMap = stockResponse.getTimeSeries();

                        if (dailyDataMap != null && !dailyDataMap.isEmpty()) {
                            processStockData(symbol, dailyDataMap);
                        }
                    }
                }
            }
        });
    }

    private void processStockData(String symbol, Map<String, StockResponse.DailyData> dailyDataMap) {
        StockResponse.DailyData latestData = dailyDataMap.values().iterator().next();
        double openPrice = Double.parseDouble(latestData.getOpen());
        double highPrice = Double.parseDouble(latestData.getHigh());
        double lowPrice = Double.parseDouble(latestData.getLow());
        double closePrice = Double.parseDouble(latestData.getClose());
        double change = closePrice - openPrice; // Calculate the change
        long volume = Long.parseLong(latestData.getVolume().replaceAll(",", ""));
        Stock stock = new Stock(symbol, openPrice, highPrice, lowPrice, closePrice, change, volume);

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                trackedStocks.add(stock);
                stockAdapter.notifyDataSetChanged();
                emptyView.setVisibility(trackedStocks.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }
}