package com.example.stockmarketapp;

import com.example.stockmarketapp.api.YahooFinanceApi;
import com.example.stockmarketapp.models.NewsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsService {
    private final YahooFinanceApi yahooFinanceApi;

    public NewsService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        yahooFinanceApi = retrofit.create(YahooFinanceApi.class);
    }

    public void fetchNewsForStock(String symbol, Callback<NewsResponse> callback) {
        Call<NewsResponse> call = yahooFinanceApi.getNewsForStock(symbol);
        call.enqueue(callback);
    }
}