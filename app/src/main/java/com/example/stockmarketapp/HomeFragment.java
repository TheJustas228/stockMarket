package com.example.stockmarketapp;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import android.widget.TextView;
import com.example.stockmarketapp.models.Stock;
import com.example.stockmarketapp.adapters.StockAdapter;
import java.util.List;


public class HomeFragment extends Fragment implements StockAdapter.OnClickListener {

    private RecyclerView trackedStocksRecyclerView;
    private TextView emptyView;
    private List<Stock> trackedStocks;
    private StockAdapter stockAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        trackedStocksRecyclerView = view.findViewById(R.id.trackedStocksRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        // Initialize the list and adapter
        trackedStocks = new ArrayList<>();
        stockAdapter = new StockAdapter(trackedStocks, this);
        trackedStocksRecyclerView.setAdapter(stockAdapter);
        trackedStocksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchTrackedStocks();
        return view;
    }

    private void fetchTrackedStocks() {
        // TODO: Fetch the list of tracked stocks (from a database, API, or mock data for now)

        // For now, let's add some mock data with volume
        trackedStocks.add(new Stock("AAPL", 150.00, 152.50, 149.00, 151.00, 5000000L)); // Fix here
        trackedStocks.add(new Stock("GOOGL", 2800.00, 2820.00, 2780.00, 2805.00, 3000000L)); // Fix here

        // Update the RecyclerView
        stockAdapter.notifyDataSetChanged();

        // Show/hide the empty view based on the list size
        emptyView.setVisibility(trackedStocks.isEmpty() ? View.VISIBLE : View.GONE);
    }
    @Override
    public void onStockClicked(Stock stock) {
        // Implement what happens when a stock is clicked
    }
}