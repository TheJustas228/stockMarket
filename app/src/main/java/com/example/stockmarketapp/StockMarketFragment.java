package com.example.stockmarketapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmarketapp.adapters.StockAdapter;
import com.example.stockmarketapp.api.ApiClient;
import com.example.stockmarketapp.api.YahooFinanceService;
import com.example.stockmarketapp.models.StockModel;
import com.example.stockmarketapp.models.StockResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockMarketFragment extends Fragment implements StockAdapter.OnClickListener {

    private RecyclerView stockMarketRecyclerView;
    private List<StockModel> stockMarketStocks;
    private StockAdapter stockAdapter;
    private YahooFinanceService yahooFinanceService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_market, container, false);
        stockMarketRecyclerView = view.findViewById(R.id.stockMarketRecyclerView);
        stockMarketStocks = new ArrayList<>();
        stockAdapter = new StockAdapter(stockMarketStocks, this);
        stockMarketRecyclerView.setAdapter(stockAdapter);
        stockMarketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        yahooFinanceService = ApiClient.getClient().create(YahooFinanceService.class);
        fetchStockMarketStocks();

        return view;
    }

    private void fetchStockMarketStocks() {
        String[] stockSymbols = {"AAPL", "MSFT", "AMZN", "TSLA", "GOOGL", "GOOG", "JNJ", "UNH", "NVDA"};
        for (String symbol : stockSymbols) {
            fetchStockData(symbol);
        }
    }

    private void fetchStockData(String symbol) {
        Call<StockResponse> call = yahooFinanceService.getStockOptions(symbol);
        call.enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StockResponse stockResponse = response.body();
                    StockResponse.Quote quote = stockResponse.getOptionChain().getResult().get(0).getQuote();

                    // Creating StockModel from the response
                    StockModel stock = new StockModel();
                    stock.setSymbol(quote.getSymbol());
                    stock.setClosePrice(quote.getRegularMarketPrice());
                    stock.setChange(quote.getRegularMarketChange());

                    // TODO: Set other properties of StockModel as required

                    stockMarketStocks.add(stock);
                    stockAdapter.notifyDataSetChanged(); // Notify adapter about data change
                } else {
                    Log.e("StockMarketFragment", "Response not successful for symbol: " + symbol);
                }
            }

            @Override
            public void onFailure(Call<StockResponse> call, Throwable t) {
                Log.e("StockMarketFragment", "Error fetching stock data for " + symbol, t);
            }
        });
    }



    @Override
    public void onStockClicked(StockModel stock) {
        StockGraphFragment stockGraphFragment = StockGraphFragment.newInstance(stock);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, stockGraphFragment)
                .addToBackStack(null)
                .commit();
    }
}