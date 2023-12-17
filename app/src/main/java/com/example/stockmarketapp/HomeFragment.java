package com.example.stockmarketapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmarketapp.adapters.StockAdapter;
import com.example.stockmarketapp.models.StockModel;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class HomeFragment extends Fragment {

    private RecyclerView trackedStocksRecyclerView;
    private TextView emptyView;
    private boolean isRemoveModeActive = false; // Initialized to false
    private Button removeButton;
    private StockAdapter stockAdapter;
    private SharedViewModel viewModel;
    private ExecutorService executorService;
    private Handler handler;
    private Handler priceUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable priceUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            fetchLatestPrices();
            priceUpdateHandler.postDelayed(this, 15000); // 30 seconds
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        handler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();
        priceUpdateHandler = new Handler(Looper.getMainLooper());
        priceUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                fetchLatestPrices();
                priceUpdateHandler.postDelayed(this, 15000); // Schedule next update after 30 seconds
            }
        };
    }
    @Override
    public void onResume() {
        super.onResume();
        priceUpdateHandler.post(priceUpdateRunnable); // Start price updates
    }

    @Override
    public void onPause() {
        super.onPause();
        priceUpdateHandler.removeCallbacks(priceUpdateRunnable); // Stop price updates
    }

    private void fetchLatestPrices() {
        List<StockModel> trackedStocks = viewModel.getTrackedStocks().getValue();
        if (trackedStocks != null) {
            for (StockModel stock : trackedStocks) {
                String symbol = stock.getSymbol();
                fetchPriceForStock(symbol); // This method should also update close price and change
            }
        }
    }

    private void fetchPriceForStock(String symbol) {
        executorService.execute(() -> {
            String urlString = "https://query1.finance.yahoo.com/v7/finance/options/" + symbol;
            Log.d("HomeFragment", "fetchPriceForStock - URL: " + urlString);
            HttpsURLConnection urlConnection = null;

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject jsonObject = new JSONObject(result.toString());
                JSONObject quote = jsonObject.getJSONObject("optionChain")
                        .getJSONArray("result")
                        .getJSONObject(0)
                        .getJSONObject("quote");

                double latestPrice = quote.getDouble("regularMarketPrice");
                double closePrice = quote.getDouble("regularMarketPreviousClose"); // Extract close price
                double change = quote.getDouble("regularMarketChange"); // Extract change

                Log.d("HomeFragment", "Fetched latest price for " + symbol + ": " + latestPrice);

                handler.post(() -> {
                    updateStockPrice(symbol, latestPrice, closePrice, change);
                });

            } catch (Exception e) {
                Log.e("HomeFragment", "Error fetching stock price: " + e.getMessage(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    private void updateStockPrice(String symbol, double latestPrice, double closePrice, double change) {
        List<StockModel> stocks = viewModel.getTrackedStocks().getValue();
        if (stocks != null) {
            for (StockModel stock : stocks) {
                if (stock.getSymbol().equals(symbol)) {
                    stock.setLatestPrice(latestPrice);
                    stock.setLatestPrice(latestPrice);
                    stock.setClosePrice(closePrice); // Set the close price
                    stock.setChange(change); // Set the change
                    Log.d("HomeFragment", "Updated latest price for " + symbol + " to " + latestPrice);
                }
            }
            handler.post(() -> stockAdapter.notifyDataSetChanged());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d("HomeFragment", "onCreateView - Creating view");
        // Initialize views
        trackedStocksRecyclerView = view.findViewById(R.id.trackedStocksRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        removeButton = view.findViewById(R.id.removeButton);
        DatabaseHelper db = new DatabaseHelper(getContext());
        List<String> trackedStockSymbols = db.getAllStocks();

        // Assuming you have a method to convert stock symbols to StockModel objects
        List<StockModel> trackedStocks = convertSymbolsToStockModels(trackedStockSymbols);
        viewModel.setTrackedStocks(trackedStocks); // Update the ViewModel with the stocks from the database

        // Initialize adapter with separate click listeners
        stockAdapter = new StockAdapter(getContext(),
                new ArrayList<>(),
                stock -> {
                    // Regular click listener
                    // Log or handle regular click here
                    Log.d("HomeFragment", "Regular click: " + stock.getSymbol());
                },
                this::removeTrackedStock,
                true
        );
        trackedStocksRecyclerView.setAdapter(stockAdapter);
        trackedStocksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getTrackedStocks().observe(getViewLifecycleOwner(), this::updateTrackedStocks);

        removeButton.setOnClickListener(v -> toggleRemoveMode());

        return view;
    }

    private List<StockModel> convertSymbolsToStockModels(List<String> symbols) {
        List<StockModel> stocks = new ArrayList<>();
        for (String symbol : symbols) {
            StockModel stock = new StockModel();
            stock.setSymbol(symbol);
            stocks.add(stock);
        }
        return stocks;
    }

    private void toggleRemoveMode() {
        isRemoveModeActive = !isRemoveModeActive;
        Log.d("HomeFragment", "Remove mode toggled. Current state: " + isRemoveModeActive);
        stockAdapter.setRemoveMode(isRemoveModeActive);
        removeButton.setText(isRemoveModeActive ? "Cancel Remove" : "Remove");
    }

    private void updateTrackedStocks(List<StockModel> stocks) {
        stockAdapter.setStocks(stocks);
        stockAdapter.notifyDataSetChanged();
        emptyView.setVisibility(stocks.isEmpty() ? View.VISIBLE : View.GONE);
        removeButton.setVisibility(stocks.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void removeTrackedStock(StockModel stock) {
        DatabaseHelper db = new DatabaseHelper(getContext());
        db.deleteStock(stock.getSymbol());
        Log.d("HomeFragment", "Removing stock: " + stock.getSymbol());
        viewModel.removeStock(stock);
        saveTrackedStocks();
    }

    private void saveTrackedStocks() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("tracked_stocks", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(viewModel.getTrackedStocks().getValue());
        editor.putString("stocks", json);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        Log.d("HomeFragment", "onDestroy - Destroying view");
        super.onDestroy();
        // Shutdown ExecutorService when the fragment is destroyed
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}