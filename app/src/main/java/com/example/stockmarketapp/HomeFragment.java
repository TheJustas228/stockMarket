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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmarketapp.adapters.StockAdapter;
import com.example.stockmarketapp.models.StockModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private DatabaseReference databaseReference;
    private DatabaseHelper databaseHelper;
    private String userId;
    private Handler priceUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable priceUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            fetchLatestPrices();
            priceUpdateHandler.postDelayed(this, 15000); // 15 seconds
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("userStocks").child(userId);
            viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

            viewModel.getTrackedStocks().observe(this, stocks -> {
                stockAdapter.setStocks(stocks);
                stockAdapter.notifyDataSetChanged();
                updateEmptyViewVisibility(stocks.isEmpty());
            });

            databaseHelper = new DatabaseHelper();
            executorService = Executors.newSingleThreadExecutor();
        } else {
            Log.e("HomeFragment", "No user is logged in");
            // Redirect to login activity or handle the user not being logged in
        }
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
        if (viewModel != null) {
            LiveData<List<StockModel>> trackedStocksLiveData = viewModel.getTrackedStocks();
            if (trackedStocksLiveData != null && trackedStocksLiveData.getValue() != null) {
                for (StockModel stock : trackedStocksLiveData.getValue()) {
                    String symbol = stock.getSymbol();
                    fetchPriceForStock(symbol);
                    Log.d("HomeFragment", "Fetching prices for stock: " + symbol);
                }
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
                double closePrice = quote.getDouble("regularMarketPreviousClose");
                double change = quote.getDouble("regularMarketChange");

                Log.d("HomeFragment", "Fetched latest price for " + symbol + ": " + latestPrice);

                // Ensure that handler is not null and the fragment is still added
                if (handler != null && isAdded()) {
                    handler.post(() -> {
                        updateStockPrice(symbol, latestPrice, closePrice, change);
                    });
                }

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

        // Initialize RecyclerView and other views
        trackedStocksRecyclerView = view.findViewById(R.id.trackedStocksRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        removeButton = view.findViewById(R.id.removeButton);
        databaseReference = databaseHelper.getUserStocksReference();

        // Initialize adapter with empty list
        stockAdapter = new StockAdapter(getContext(), new ArrayList<>(), this::onStockClicked, this::removeTrackedStock);
        trackedStocksRecyclerView.setAdapter(stockAdapter);
        trackedStocksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch tracked stocks
        fetchTrackedStocks();

        // Set up the remove mode toggle
        removeButton.setOnClickListener(v -> toggleRemoveMode());

        return view;
    }

    private void fetchTrackedStocks() {
        if (userId != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<StockModel> stocks = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String symbol = snapshot.getKey();
                        if (symbol != null) {
                            StockModel stock = new StockModel();
                            stock.setSymbol(symbol);
                            stocks.add(stock);
                        }
                    }
                    viewModel.setTrackedStocks(stocks);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("HomeFragment", "Failed to read value.", databaseError.toException());
                }
            });
        }
    }

    private void onStockClicked(StockModel stock) {
        // Implement what happens when a stock is clicked
    }

    private void updateEmptyViewVisibility(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        removeButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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
        boolean isRemoveModeActive = stockAdapter.isRemoveModeActive();
        stockAdapter.setRemoveMode(!isRemoveModeActive);
        removeButton.setText(getString(isRemoveModeActive ? R.string.remove : R.string.cancel_remove));
    }

    private void updateTrackedStocks(List<StockModel> stocks) {
        stockAdapter.setStocks(stocks);
        stockAdapter.notifyDataSetChanged();
        emptyView.setVisibility(stocks.isEmpty() ? View.VISIBLE : View.GONE);
        removeButton.setVisibility(stocks.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void removeTrackedStock(StockModel stock) {
        databaseReference.child(stock.getSymbol()).removeValue()
                .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Stock removed: " + stock.getSymbol()))
                .addOnFailureListener(e -> Log.e("HomeFragment", "Failed to remove stock: " + stock.getSymbol(), e));
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