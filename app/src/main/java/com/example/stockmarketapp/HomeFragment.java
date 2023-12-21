package com.example.stockmarketapp;

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

    private TextView emptyView;
    private Button removeButton;
    private StockAdapter stockAdapter;
    private SharedViewModel viewModel;
    private ExecutorService executorService;
    private Handler handler;
    private DatabaseReference databaseReference;
    private DatabaseHelper databaseHelper;
    private final Handler priceUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable priceUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            fetchLatestPrices();
            priceUpdateHandler.postDelayed(this, 15000);
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
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
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        priceUpdateHandler.post(priceUpdateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        priceUpdateHandler.removeCallbacks(priceUpdateRunnable);
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

                if (handler != null && isAdded()) {
                    handler.post(() -> updateStockPrice(symbol, latestPrice, closePrice, change));
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
                    stock.setClosePrice(closePrice);
                    stock.setChange(change);
                    Log.d("HomeFragment", "Updated latest price for " + symbol + " to " + latestPrice);
                }
            }
            handler.post(() -> stockAdapter.notifyDataSetChanged());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView trackedStocksRecyclerView = view.findViewById(R.id.trackedStocksRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        removeButton = view.findViewById(R.id.removeButton);

        databaseHelper = new DatabaseHelper();
        databaseReference = databaseHelper.getDatabaseReference();

        stockAdapter = new StockAdapter(getContext(), new ArrayList<>(), this::onStockClicked, this::removeTrackedStock);
        trackedStocksRecyclerView.setAdapter(stockAdapter);
        trackedStocksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchTrackedStocks();

        removeButton.setOnClickListener(v -> toggleRemoveMode());

        return view;
    }


    private void fetchTrackedStocks() {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        DatabaseReference userStocksReference = databaseHelper.getDatabaseReference();

        if (userStocksReference != null) {
            userStocksReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("HomeFragment", "Failed to read value.", databaseError.toException());
                }
            });
        }
    }


    private void onStockClicked(StockModel stock) {
    }

    private void updateEmptyViewVisibility(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        removeButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void toggleRemoveMode() {
        boolean isRemoveModeActive = stockAdapter.isRemoveModeActive();
        stockAdapter.setRemoveMode(!isRemoveModeActive);
        removeButton.setText(getString(isRemoveModeActive ? R.string.remove : R.string.cancel_remove));
    }

    private void removeTrackedStock(StockModel stock) {
        databaseReference.child(stock.getSymbol()).removeValue()
                .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Stock removed: " + stock.getSymbol()))
                .addOnFailureListener(e -> Log.e("HomeFragment", "Failed to remove stock: " + stock.getSymbol(), e));
    }

    @Override
    public void onDestroy() {
        Log.d("HomeFragment", "onDestroy - Destroying view");
        super.onDestroy();

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}