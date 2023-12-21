package com.example.stockmarketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stockmarketapp.models.StockModel;

public class StockDetailFragment extends Fragment {

    private StockModel stock;

    public StockDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stock = (StockModel) getArguments().getSerializable("stock");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_stock_detail, container, false);

        ((TextView) view.findViewById(R.id.tvSymbol)).setText("Symbol: " + stock.getSymbol());
        ((TextView) view.findViewById(R.id.tvPrice)).setText("Price: $" + stock.getClosePrice());
        ((TextView) view.findViewById(R.id.tvChange)).setText("Change: $" + stock.getChange());
        ((TextView) view.findViewById(R.id.tvChangePercent)).setText("Change (%): " + stock.getChangePercent() + "%");

        return view;
    }
}