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

public class StockMarketFragment extends Fragment implements StockAdapter.OnClickListener {

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
        stockAdapter = new StockAdapter(stockMarketStocks, this);
        stockMarketRecyclerView.setAdapter(stockAdapter);
        stockMarketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchStockMarketStocks();

        return view;
    }

    private void fetchStockMarketStocks() {
        // Top 10 stocks from the S&P 500 list (example symbols)
        String[] stockSymbols = {"AAPL", "MSFT", "AMZN", "TSLA", "GOOGL", "GOOG", "BRK.B", "JNJ", "UNH", "NVDA"};

        for (String symbol : stockSymbols) {
            fetchStockData(symbol);
        }
    }

    private void fetchStockData(String symbol) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=CZX3RFB37AMFOXWL";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("StockMarketFragment", "Error fetching data for symbol: " + symbol, e);
                updateUIForError("Error fetching stock data from server.");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("StockMarketFragment", "Unsuccessful response for symbol: " + symbol);
                    updateUIForError("Error fetching stock data from server.");
                    return;
                }

                String jsonResponse = response.body() != null ? response.body().string() : null;
                Log.d("StockMarketFragment", "Response for symbol " + symbol + ": " + jsonResponse);
                Gson gson = new Gson();
                StockResponse stockResponse = gson.fromJson(jsonResponse, StockResponse.class);

                if (stockResponse != null && stockResponse.getTimeSeries() != null) {
                    Map<String, StockResponse.DailyData> dailyDataMap = stockResponse.getTimeSeries();
                    if (dailyDataMap != null && !dailyDataMap.isEmpty()) {
                        processStockData(symbol, dailyDataMap);
                    } else {
                        updateUIForError("No data available for symbol: " + symbol);
                    }
                } else {
                    updateUIForError("Invalid response for symbol: " + symbol);
                }
            }
        });
    }

    private void updateUIForError(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                emptyViewStockMarket.setText(errorMessage);
                emptyViewStockMarket.setVisibility(View.VISIBLE);
            });
        }
    }
    private double parseDoubleSafely(String numberStr) {
        try {
            return numberStr != null ? Double.parseDouble(numberStr.trim()) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private long parseLongSafely(String numberStr) {
        try {
            return numberStr != null ? Long.parseLong(numberStr.trim().replaceAll(",", "")) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    private void processStockData(String symbol, Map<String, StockResponse.DailyData> dailyDataMap) {
        for (Map.Entry<String, StockResponse.DailyData> entry : dailyDataMap.entrySet()) {
            StockResponse.DailyData dailyData = entry.getValue();
            if (dailyData != null) {
                String date = entry.getKey();
                double openPrice = parseDoubleSafely(dailyData.getOpen());
                double highPrice = parseDoubleSafely(dailyData.getHigh());
                double lowPrice = parseDoubleSafely(dailyData.getLow());
                double closePrice = parseDoubleSafely(dailyData.getClose());
                double change = closePrice - openPrice; // Calculate the change
                long volume = parseLongSafely(dailyData.getVolume());
                Stock stock = new Stock(date, openPrice, highPrice, lowPrice, closePrice, change, volume);
                stockMarketStocks.add(stock);
            }
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                stockAdapter.notifyDataSetChanged();
                emptyViewStockMarket.setVisibility(View.GONE);
            });
        }
    }

    @Override
    public void onStockClicked(Stock stock) {
        StockDetailFragment stockDetailFragment = StockDetailFragment.newInstance(stock);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, stockDetailFragment)
                .addToBackStack(null)
                .commit();
    }
}