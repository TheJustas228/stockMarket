package com.example.stockmarketapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

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

public class StockMarketFragment extends Fragment {

    private List<StockModel> stockMarketStocks;
    private List<StockModel> originalStockMarketStocks;
    private StockAdapter stockAdapter;
    private YahooFinanceService yahooFinanceService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_market, container, false);
        RecyclerView stockMarketRecyclerView = view.findViewById(R.id.stockMarketRecyclerView);
        stockMarketStocks = new ArrayList<>();
        originalStockMarketStocks = new ArrayList<>();

        stockAdapter = new StockAdapter(getContext(), stockMarketStocks, this::onStockSelected, stock -> {});
        stockMarketRecyclerView.setAdapter(stockAdapter);
        stockMarketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        yahooFinanceService = ApiClient.getClient().create(YahooFinanceService.class);
        fetchStockMarketStocks();

        Button btnSort = view.findViewById(R.id.btnSort);
        btnSort.setOnClickListener(v -> showSortOptionsDialog());

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterStocks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStocks(newText);
                return true;
            }
        });

        return view;
    }

    private void filterStocks(String query) {
        List<StockModel> filteredList = new ArrayList<>();
        for (StockModel stock : originalStockMarketStocks) {
            if (stock.getSymbol().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(stock);
            }
        }
        stockMarketStocks.clear();
        stockMarketStocks.addAll(filteredList);
        stockAdapter.notifyDataSetChanged();
    }

    private void showSortOptionsDialog() {
        String[] sortOptions = {"Price Ascending", "Price Descending", "Change Ascending", "Change Descending", "Close Price Ascending", "Close Price Descending"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort by")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: sortStocks(SortType.PRICE_ASCENDING); break;
                        case 1: sortStocks(SortType.PRICE_DESCENDING); break;
                        case 2: sortStocks(SortType.CHANGE_ASCENDING); break;
                        case 3: sortStocks(SortType.CHANGE_DESCENDING); break;
                        case 4: sortStocks(SortType.CLOSE_PRICE_ASCENDING); break;
                        case 5: sortStocks(SortType.CLOSE_PRICE_DESCENDING); break;
                    }
                });
        builder.create().show();
    }

    private void sortStocks(SortType sortType) {
        Log.d("StockMarketFragment", "Sorting stocks by: " + sortType.name());
        switch (sortType) {
            case PRICE_ASCENDING:
                stockMarketStocks.sort((o1, o2) -> {
                    Log.d("Sort", "Comparing (Price Ascending): " + o1.getLatestPrice() + " - " + o2.getLatestPrice());
                    return Double.compare(o1.getLatestPrice(), o2.getLatestPrice());
                });
                break;
            case PRICE_DESCENDING:
                stockMarketStocks.sort((o1, o2) -> {
                    Log.d("Sort", "Comparing (Price Descending): " + o1.getLatestPrice() + " - " + o2.getLatestPrice());
                    return Double.compare(o2.getLatestPrice(), o1.getLatestPrice());
                });
                break;
            case CHANGE_ASCENDING:
                stockMarketStocks.sort((o1, o2) -> {
                    double changePercent1 = o1.getClosePrice() != 0 ? (o1.getChange() / o1.getClosePrice()) * 100 : 0;
                    double changePercent2 = o2.getClosePrice() != 0 ? (o2.getChange() / o2.getClosePrice()) * 100 : 0;
                    Log.d("Sort", "Comparing (Change Ascending): " + changePercent1 + "% - " + changePercent2 + "%");
                    return Double.compare(changePercent1, changePercent2);
                });
                break;
            case CHANGE_DESCENDING:
                stockMarketStocks.sort((o1, o2) -> {
                    double changePercent1 = o1.getClosePrice() != 0 ? (o1.getChange() / o1.getClosePrice()) * 100 : 0;
                    double changePercent2 = o2.getClosePrice() != 0 ? (o2.getChange() / o2.getClosePrice()) * 100 : 0;
                    Log.d("Sort", "Comparing (Change Descending): " + changePercent1 + "% - " + changePercent2 + "%");
                    return Double.compare(changePercent2, changePercent1);
                });
                break;
            case CLOSE_PRICE_ASCENDING:
                stockMarketStocks.sort((o1, o2) -> {
                    Log.d("Sort", "Comparing (Close Ascending): " + o1.getClosePrice() + " - " + o2.getClosePrice());
                    return Double.compare(o1.getClosePrice(), o2.getClosePrice());
                });
                break;
            case CLOSE_PRICE_DESCENDING:
                stockMarketStocks.sort((o1, o2) -> {
                    Log.d("Sort", "Comparing (Close Descending): " + o1.getClosePrice() + " - " + o2.getClosePrice());
                    return Double.compare(o2.getClosePrice(), o1.getClosePrice());
                });
                break;
        }
        stockAdapter.notifyDataSetChanged();
    }

    private void onStockSelected(StockModel stock) {
        StockGraphFragment stockGraphFragment = StockGraphFragment.newInstance(stock);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, stockGraphFragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchStockMarketStocks() {
        String[] stockSymbols = {"aapl", "msft", "amzn", "tsla", "googl", "goog", "jnj", "unh", "nvda"};
        for (String symbol : stockSymbols) {
            fetchStockData(symbol);
        }
    }

    private void fetchStockData(String symbol) {
        Call<StockResponse> call = yahooFinanceService.getStockOptions(symbol);
        call.enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(@NonNull Call<StockResponse> call, @NonNull Response<StockResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StockResponse stockResponse = response.body();
                    StockResponse.Quote quote = stockResponse.getOptionChain().getResult().get(0).getQuote();

                    double closePrice = quote.getRegularMarketPreviousClose();
                    double latestPrice = quote.getRegularMarketPrice();
                    double change = quote.getRegularMarketChangePercent();

                    StockModel stock = new StockModel();
                    stock.setSymbol(quote.getSymbol());
                    stock.setClosePrice(closePrice);
                    stock.setLatestPrice(latestPrice);
                    stock.setChange(change);
                    originalStockMarketStocks.add(stock);
                    stockMarketStocks.add(stock);

                    stockAdapter.notifyDataSetChanged();
                } else {
                    Log.e("StockMarketFragment", "Response not successful for symbol: " + symbol);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StockResponse> call, @NonNull Throwable t) {
                Log.e("StockMarketFragment", "Error fetching stock data for " + symbol, t);
            }
        });
    }
}