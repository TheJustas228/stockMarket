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
        // TODO: Fetch the list of stocks for the stock market (from an API, database, or mock data for now)

        // For now, let's add some mock data
        stockMarketStocks.add(new Stock("AAPL", 150.00, 2.50));
        stockMarketStocks.add(new Stock("GOOGL", 2800.00, -15.00));
        stockMarketStocks.add(new Stock("AMZN", 3300.00, 25.00));

        stockAdapter.notifyDataSetChanged();

        emptyViewStockMarket.setVisibility(stockMarketStocks.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
