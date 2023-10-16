//C:\Users\User\AndroidStudioProjects\StockMarketApp\app\src\main\java\com\example\stockmarketapp\StockMarketFragment.java
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
import com.example.stockmarketapp.api.ApiClient;
import com.example.stockmarketapp.api.AlphaVantageService;
import com.example.stockmarketapp.models.StockResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        AlphaVantageService service = ApiClient.getService();
        Call<StockResponse> call = service.getStockInfo("TIME_SERIES_DAILY", "IBM", "CZX3RFB37AMFOXWL");
        call.enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: Extract stock data from response.body() and update UI
                    // Example: String openPrice = response.body().getTimeSeries().getDailyData().getOpen();
                } else {
                    // TODO: Handle error - show error message or another appropriate feedback to the user
                }
            }

            @Override
            public void onFailure(Call<StockResponse> call, Throwable t) {
                // TODO: Handle failure - show error message or another appropriate feedback to the user
            }
        });
    }
}
