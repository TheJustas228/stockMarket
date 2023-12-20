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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.stockmarketapp.adapters.NewsAdapter;
import com.example.stockmarketapp.models.NewsResponse;
import com.example.stockmarketapp.models.StockModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsFragment extends Fragment {
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private Map<String, Integer> articleCountPerStock;

    private TextView tvNoStocksMessage;
    private SharedViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView);
        tvNoStocksMessage = view.findViewById(R.id.tvNoStocksMessage);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

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
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsArticle> articles = response.body().getNews();
                    Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                    if (articles != null) {
                        Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                        for (NewsArticle article : articles) {
                            Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                            if (articleCountPerStock.get(symbol) < 2) {
                                article.setSymbol(symbol); // Set the symbol for each article
                                newsAdapter.addNewsArticle(article);
                                articleCountPerStock.put(symbol, articleCountPerStock.get(symbol) + 1);
                                Log.d("NewsFragment", "Fetching news for stock: " + symbol);
                            }
                        }
                    }

                    getActivity().runOnUiThread(() -> newsAdapter.notifyDataSetChanged());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {

                Log.e("NewsFragment", "Error fetching news for stock: " + symbol, t);
            }
        });
    }
}