package com.example.stockmarketapp.api;

import com.example.stockmarketapp.models.StockResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AlphaVantageService {
    @GET("query")
    Call<StockResponse> getStockData(@Query("function") String function,
                                     @Query("symbol") String symbol,
                                     @Query("apikey") String apiKey);
}
