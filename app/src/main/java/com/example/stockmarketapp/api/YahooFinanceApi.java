package com.example.stockmarketapp.api;

import com.example.stockmarketapp.models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YahooFinanceApi {
    @GET("/v1/finance/search")
    Call<NewsResponse> getNewsForStock(@Query("q") String stockSymbol);
}