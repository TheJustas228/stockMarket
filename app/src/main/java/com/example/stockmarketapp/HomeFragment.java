package com.example.stockmarketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockmarketapp.adapters.StockAdapter;
import com.example.stockmarketapp.models.StockModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;

import yahoofinance.YahooFinance;

public class HomeFragment extends Fragment {

    private RecyclerView trackedStocksRecyclerView;
    private TextView emptyView;
    private List<StockModel> trackedStocks;
    private StockAdapter stockAdapter;

    private SharedViewModel viewModel;

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

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getTrackedStocks().observe(getViewLifecycleOwner(), updatedStocks -> {
            trackedStocks.clear();
            trackedStocks.addAll(updatedStocks);
            stockAdapter.notifyDataSetChanged();
            emptyView.setVisibility(trackedStocks.isEmpty() ? View.VISIBLE : View.GONE);
        });

        return view;
    }

    private void fetchTrackedStocks() {
        List<StockModel> stocks = TrackedStocksManager.getInstance().getTrackedStocks();
        trackedStocks.clear();
        trackedStocks.addAll(stocks);
        stockAdapter.notifyDataSetChanged();
    }

    public void addTrackedStock(StockModel stock) {
        if (!trackedStocks.contains(stock)) {
            trackedStocks.add(stock);
            stockAdapter.notifyDataSetChanged();
            saveTrackedStocks(); // Save to persistent storage
        }
    }

    private void saveTrackedStocks() {
        // Implement logic to save tracked stocks (e.g., using SharedPreferences)
    }

    private void fetchStockData(String symbol) {
        new Thread(() -> {
            try {
                yahoofinance.Stock stock = YahooFinance.get(symbol);
                StockModel stockModel = new StockModel(stock);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        trackedStocks.add(stockModel);
                        stockAdapter.notifyDataSetChanged();
                        emptyView.setVisibility(trackedStocks.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Handle error
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        emptyView.setText("Error fetching stock data.");
                        emptyView.setVisibility(View.VISIBLE);
                    });
                }
            }
        }).start();
    }
}