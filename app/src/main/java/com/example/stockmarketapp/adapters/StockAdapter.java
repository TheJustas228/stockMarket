package com.example.stockmarketapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmarketapp.DatabaseHelper;
import com.example.stockmarketapp.R;
import com.example.stockmarketapp.models.StockModel;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<StockModel> stocks;
    private boolean isRemoveModeActive = false;
    private OnRemoveButtonClickListener onRemoveButtonClickListener;
    private final OnItemClickListener onItemClickListener;
    private boolean isRemoveButtonVisible = false;
    private boolean showLatestPrice;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(StockModel stock);
    }

    public void onRemoveButtonClicked(StockModel stock) {
        if (stock != null) {
            DatabaseHelper db = new DatabaseHelper(context);
            db.deleteStock(stock.getSymbol());
            Log.d("StockAdapter", "Stock removed: " + stock.getSymbol());
            // Update the ViewModel and UI accordingly
        }
    }

    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClicked(StockModel stock);
    }

    public StockAdapter(Context context, List<StockModel> stocks, OnItemClickListener onItemClickListener, OnRemoveButtonClickListener onRemoveButtonClickListener) {
        this.context = context;
        this.stocks = stocks;
        this.onItemClickListener = onItemClickListener;
        this.onRemoveButtonClickListener = onRemoveButtonClickListener;
        this.showLatestPrice = true; // Always show the latest price
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

        // Set the stock name and price
        holder.stockNameTextView.setText(stock.getSymbol());
        holder.stockPrice.setText(String.format("Close Price: $%.2f", stock.getClosePrice()));

        // Calculate and display the change as a percentage
        if (stock.getClosePrice() != 0) {
            double changePercent = (stock.getChange() / stock.getClosePrice()) * 100;
            holder.stockChange.setText(String.format("Change: %.2f%%", changePercent));
            holder.stockChange.setTextColor(changePercent >= 0 ? Color.GREEN : Color.RED);
        } else {
            holder.stockChange.setText("Change: N/A");
            holder.stockChange.setTextColor(Color.GRAY);
        }

        // Update the latest price
        holder.stockLatestPriceTextView.setText(String.format("Latest Price: $%.2f", stock.getLatestPrice()));
        holder.stockLatestPriceTextView.setVisibility(showLatestPrice ? View.VISIBLE : View.GONE);

        // Set click listeners for item and remove button
        holder.itemView.setOnClickListener(v -> {
            if (isRemoveModeActive) {
                if (onRemoveButtonClickListener != null) {
                    onRemoveButtonClickListener.onRemoveButtonClicked(stock);
                    removeItem(position);
                }
            } else {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(stock);
                }
            }
        });

        if (holder.removeButton != null) {
            holder.removeButton.setVisibility(isRemoveButtonVisible ? View.VISIBLE : View.GONE);
            holder.removeButton.setOnClickListener(v -> {
                onRemoveButtonClickListener.onRemoveButtonClicked(stock);
                removeItem(position);
            });
        }
    }

    public void setStocks(List<StockModel> stocks) {
        this.stocks = stocks;
    }

    private void removeItem(int position) {
        if (position >= 0 && position < stocks.size()) {
            stocks.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, stocks.size());
        } else {
            Log.e("StockAdapter", "Attempted to remove item at invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, stockPrice, stockChange, stockNameTextView, stockLatestPriceTextView;
        Button removeButton;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            stockNameTextView = itemView.findViewById(R.id.stockName); // Replace with your ID
            stockLatestPriceTextView = itemView.findViewById(R.id.stockLatestPrice); // ID of the TextView in stock_item.xml
            stockName = itemView.findViewById(R.id.stockName);
            stockPrice = itemView.findViewById(R.id.stockPrice);
            stockChange = itemView.findViewById(R.id.stockChange);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}