// app/src/main/java/com/example/stockmarketapp/api/ApiClient.java
package com.example.stockmarketapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://www.alphavantage.co/";
    private static Retrofit retrofit = null;

    public static AlphaVantageService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(AlphaVantageService.class);
    }
}
