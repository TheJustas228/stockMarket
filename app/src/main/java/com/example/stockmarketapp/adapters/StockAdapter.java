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
        // Update these lines to match the new Stock model
        holder.stockName.setText(stock.getSymbol());  // Update the method getName() as per the new model
        holder.stockPrice.setText(String.format("Close Price: $%.2f", stock.getClosePrice()));
        holder.stockChange.setText(String.format("Change: $%.2f", stock.getChange()));
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