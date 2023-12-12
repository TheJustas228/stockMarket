package com.example.stockmarketapp.api;

import com.example.stockmarketapp.models.StockResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface YahooFinanceService {
    @GET("finance/options/{symbol}")
    Call<StockResponse> getStockOptions(@Path("symbol") String symbol);

    @GET("v8/finance/chart/{symbol}")
    Call<StockResponse> getHistoricalData(
            @Path("symbol") String symbol,
            @Query("interval") String interval,
            @Query("range") String range
    );
}