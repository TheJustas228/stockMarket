package com.example.stockmarketapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockmarketapp.R;
import com.example.stockmarketapp.models.StockModel;
import java.util.List;
import android.content.res.Resources;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import com.example.stockmarketapp.R;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private StockModel stock;
    private OnClickListener onClickListener;
    private List<StockModel> stocks;

    public interface OnClickListener {
        void onStockClicked(StockModel stock);
    }
    public StockAdapter(List<StockModel> stocks, OnClickListener onClickListener) {
        this.stocks = stocks;
        this.onClickListener = onClickListener;
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
        double change = stock.getChange();
        holder.stockChange.setText(String.format("Change: $%.2f", change));
        holder.stockChange.setTextColor(change >= 0 ? Color.GREEN : Color.RED);

        holder.itemView.setOnClickListener(v -> onClickListener.onStockClicked(stock));
    }


    @Override
    public int getItemCount() {
        return stocks.size();
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, stockPrice, stockChange;

        StockViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stockName);
            stockPrice = itemView.findViewById(R.id.stockPrice);
            stockChange = itemView.findViewById(R.id.stockChange);
        }
    }
}