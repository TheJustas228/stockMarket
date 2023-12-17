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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.stockmarketapp.adapters.NewsAdapter;
import com.example.stockmarketapp.models.NewsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsFragment extends Fragment {
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private DatabaseHelper databaseHelper;
    private Map<String, Integer> articleCountPerStock;

    private TextView tvNoStocksMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView);
        tvNoStocksMessage = view.findViewById(R.id.tvNoStocksMessage);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseHelper = new DatabaseHelper(getContext());
        List<String> trackedStocks = databaseHelper.getAllStocks();
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
        service.fetchNewsForStock(symbol, new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsArticle> articles = response.body().getNews();
                    if (articles != null) {
                        for (NewsArticle article : articles) {
                            if (articleCountPerStock.get(symbol) < 2) {
                                article.setSymbol(symbol); // Set the symbol for each article
                                newsAdapter.addNewsArticle(article);
                                articleCountPerStock.put(symbol, articleCountPerStock.get(symbol) + 1);
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