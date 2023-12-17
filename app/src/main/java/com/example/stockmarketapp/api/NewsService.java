package com.example.stockmarketapp.api;

import com.example.stockmarketapp.models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsService {
    @GET("v1/finance/search")
    Call<NewsResponse> fetchNewsForStock(@Query("q") String symbol);
}