package com.example.stockmarketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stockmarketapp.models.Stock;

public class StockDetailFragment extends Fragment {

    private Stock stock;

    public StockDetailFragment() {
        // Required empty public constructor
    }

    // Factory method to create a new instance of this fragment using the provided parameters.
    public static StockDetailFragment newInstance(Stock stock) {
        StockDetailFragment fragment = new StockDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("stock", stock);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stock = (Stock) getArguments().getSerializable("stock");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_stock_detail, container, false);

        // Initialize TextViews and set their text to the stock details
        ((TextView) view.findViewById(R.id.tvDate)).setText("Date: " + stock.getName());
        ((TextView) view.findViewById(R.id.tvOpenPrice)).setText("Open: $" + stock.getOpenPrice());
        ((TextView) view.findViewById(R.id.tvHighPrice)).setText("High: $" + stock.getHighPrice());
        ((TextView) view.findViewById(R.id.tvLowPrice)).setText("Low: $" + stock.getLowPrice());
        ((TextView) view.findViewById(R.id.tvClosePrice)).setText("Close: $" + stock.getClosePrice());
        ((TextView) view.findViewById(R.id.tvVolume)).setText("Volume: " + stock.getVolume());

        return view;
    }
}
