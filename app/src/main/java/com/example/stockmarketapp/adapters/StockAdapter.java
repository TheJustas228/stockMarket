package com.example.stockmarketapp.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmarketapp.R;
import com.example.stockmarketapp.models.StockModel;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<StockModel> stocks;
    private boolean isRemoveModeActive = false;
    private OnRemoveButtonClickListener onRemoveButtonClickListener;
    private boolean isRemoveButtonVisible;

    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(StockModel stock);
    }

    // Define an interface for click listener
    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClicked(StockModel stock);
    }

    public StockAdapter(List<StockModel> stocks, OnItemClickListener onItemClickListener, OnRemoveButtonClickListener onRemoveButtonClickListener) {
        this.stocks = stocks;
        this.onItemClickListener = onItemClickListener;
        this.onRemoveButtonClickListener = onRemoveButtonClickListener;
    }

    public void setRemoveMode(boolean active) {
        isRemoveModeActive = active;
        Log.d("StockAdapter", "Set remove mode: " + isRemoveModeActive);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockModel stock = stocks.get(position);
        holder.stockName.setText(stock.getSymbol());
        holder.stockPrice.setText(String.format("Close Price: $%.2f", stock.getClosePrice()));
        holder.stockChange.setText(String.format("Change: $%.2f", stock.getChange()));
        holder.stockChange.setTextColor(stock.getChange() >= 0 ? Color.GREEN : Color.RED);

        holder.itemView.setOnClickListener(v -> {
            Log.d("StockAdapter", "Item clicked. Remove mode: " + isRemoveModeActive);
            if (isRemoveModeActive) {
                if (onRemoveButtonClickListener != null) {
                    Log.d("StockAdapter", "Removing item: " + stock.getSymbol());
                    onRemoveButtonClickListener.onRemoveButtonClicked(stock);
                    stocks.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, stocks.size());
                }
            } else {
                if (onItemClickListener != null) {
                    Log.d("StockAdapter", "Regular item click: " + stock.getSymbol());
                    onItemClickListener.onItemClick(stock);
                }
            }
        });
        if (holder.removeButton != null) {
            if (isRemoveButtonVisible) {
                holder.removeButton.setVisibility(View.VISIBLE);
                holder.removeButton.setOnClickListener(v -> {
                    onRemoveButtonClickListener.onRemoveButtonClicked(stock);
                    stocks.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, stocks.size());
                });
            } else {
                holder.removeButton.setVisibility(View.GONE);
            }
        }
    }

    public void setStocks(List<StockModel> stocks) {
        this.stocks = stocks;
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, stockPrice, stockChange;
        Button removeButton;

        StockViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stockName);
            stockPrice = itemView.findViewById(R.id.stockPrice);
            stockChange = itemView.findViewById(R.id.stockChange);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}