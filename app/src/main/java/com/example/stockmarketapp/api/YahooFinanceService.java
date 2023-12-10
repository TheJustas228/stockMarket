package com.example.stockmarketapp.api;

import com.example.stockmarketapp.models.StockResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YahooFinanceService {

    @GET("quote")
    Call<StockResponse> getStockQuote(@Query("symbols") String symbol);

    // Add more methods as needed
}