package com.example.stockmarketapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.stockmarketapp.models.StockModel; // Import the StockModel class

public class StockGraphFragment extends Fragment {

    private static final String ARG_STOCK = "stock";
    private StockModel stock; // Field to hold the stock model

    public StockGraphFragment() {
        // Required empty public constructor
    }

    public static StockGraphFragment newInstance(StockModel stock) {
        StockGraphFragment fragment = new StockGraphFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STOCK, stock);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stock = (StockModel) getArguments().getSerializable(ARG_STOCK);
            // Fetch data for the graph based on the stock symbol
            // Additional initialization logic can be added here
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Add code to setup the graph and other UI components
        return inflater.inflate(R.layout.fragment_stock_graph, container, false);
    }

    // Additional methods for the fragment can be added here
}