package com.example.stockmarketapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmarketapp.adapters.NewsAdapter;
import com.example.stockmarketapp.models.NewsResponse;
import com.example.stockmarketapp.models.StockModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment {
    private NewsAdapter newsAdapter;
    private Map<String, Integer> articleCountPerStock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        RecyclerView newsRecyclerView = view.findViewById(R.id.newsRecyclerView);
        TextView tvNoStocksMessage = view.findViewById(R.id.tvNoStocksMessage);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        List<String> trackedStocks = new ArrayList<>();
        if (viewModel.getTrackedStocks().getValue() != null) {
            for (StockModel stockModel : viewModel.getTrackedStocks().getValue()) {
                trackedStocks.add(stockModel.getSymbol());
            }
        }

        articleCountPerStock = new HashMap<>();
        newsAdapter = new NewsAdapter(new ArrayList<>());
        newsRecyclerView.setAdapter(newsAdapter);

        if (trackedStocks.isEmpty()) {
            tvNoStocksMessage.setVisibility(View.VISIBLE);
            newsRecyclerView.setVisibility(View.GONE);
        } else {
            tvNoStocksMessage.setVisibility(View.GONE);
            newsRecyclerView.setVisibility(View.VISIBLE);
            for (String symbol : trackedStocks) {
                articleCountPerStock.put(symbol, 0);
                fetchNewsForStock(symbol);
            }
        }

        return view;
    }

    private void fetchNewsForStock(String symbol) {
        NewsService service = new NewsService();
        Log.d("NewsFragment", "Fetching news for stock: " + symbol);
        service.fetchNewsForStock(symbol, new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsArticle> articles = response.body().getNews();
                    Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                    if (articles != null) {
                        Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                        for (NewsArticle article : articles) {
                            Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                            if (articleCountPerStock.get(symbol) < 2) {
                                article.setSymbol(symbol);
                                newsAdapter.addNewsArticle(article);
                                articleCountPerStock.put(symbol, articleCountPerStock.get(symbol) + 1);
                                Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                            }
                        }
                    }

                    requireActivity().runOnUiThread(() -> newsAdapter.notifyDataSetChanged());
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {

                Log.e("NewsFragment", "Error fetching news for stock: " + symbol, t);
            }
        });
    }
}