package com.example.stockmarketapp.api;

import com.example.stockmarketapp.models.StockResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface YahooFinanceService {
    @GET("finance/options/{symbol}")
    Call<StockResponse> getStockOptions(@Path("symbol") String symbol);
}