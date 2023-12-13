package com.example.stockmarketapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.example.stockmarketapp.models.StockModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class StockGraphFragment extends Fragment {

    private static final String ARG_STOCK = "stock";
    private StockModel stock; // Field to hold the stock model
    private LineChart chart;
    private ExecutorService executorService;
    private Handler handler;

    public StockGraphFragment() {
        // Required empty public constructor
    }

    public static StockGraphFragment newInstance(StockModel stock) {
        StockGraphFragment fragment = new StockGraphFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STOCK, stock);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stock = (StockModel) getArguments().getSerializable(ARG_STOCK);
        }
        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_graph, container, false);
        chart = view.findViewById(R.id.chart);
        setupButtonListeners(view);
        fetchStockData(stock.getSymbol(), "5m", "1d"); // Set default interval and range
        return view;
    }

    private void setupButtonListeners(View view) {
        view.findViewById(R.id.btnOneHour).setOnClickListener(v -> fetchStockData(stock.getSymbol(), "5m", "1h"));
        view.findViewById(R.id.btnOneDay).setOnClickListener(v -> fetchStockData(stock.getSymbol(), "1h", "1d"));
        view.findViewById(R.id.btnOneWeek).setOnClickListener(v -> fetchStockData(stock.getSymbol(), "90m", "1wk"));
        view.findViewById(R.id.btnOneMonth).setOnClickListener(v -> fetchStockData(stock.getSymbol(), "1d", "1mo"));
        view.findViewById(R.id.btnThreeMonths).setOnClickListener(v -> fetchStockData(stock.getSymbol(), "1wk", "3mo"));
    }

    private void fetchStockData(String symbol, String interval, String range) {
        String urlString = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=" + interval + "&range=" + range;

        executorService.execute(() -> {
            List<Entry> entries = new ArrayList<>();
            HttpsURLConnection urlConnection = null;

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:78.0) Gecko/20100101 Firefox/78.0");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(result.toString());
                    JSONObject chartObject = jsonObject.getJSONObject("chart");
                    JSONArray resultArray = chartObject.getJSONArray("result");
                    JSONObject firstResult = resultArray.getJSONObject(0);
                    JSONObject indicators = firstResult.getJSONObject("indicators");
                    JSONObject quoteObject = indicators.getJSONArray("quote").getJSONObject(0);
                    JSONArray closeArray = quoteObject.getJSONArray("close");
                    JSONArray timestampArray = firstResult.getJSONArray("timestamp");

                    for (int i = 0; i < closeArray.length(); i++) {
                        if (!closeArray.isNull(i)) {
                            float closeValue = (float) closeArray.getDouble(i);
                            long timestamp = timestampArray.getLong(i) * 1000; // Convert timestamp to milliseconds
                            entries.add(new Entry(timestamp, closeValue));
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                Log.e("StockGraphFragment", "URL not found: " + urlString, e);
                if (urlConnection != null) {
                    try {
                        int responseCode = urlConnection.getResponseCode();
                        String responseMessage = urlConnection.getResponseMessage();
                        Log.e("StockGraphFragment", "HTTP Error: " + responseCode + " - " + responseMessage);
                    } catch (IOException ex) {
                        Log.e("StockGraphFragment", "Error getting response code", ex);
                    }
                }
            } catch (IOException e) {
                Log.e("StockGraphFragment", "Error reading from URL: " + urlString, e);
            } catch (JSONException e) {
                Log.e("StockGraphFragment", "Error parsing JSON response", e);
            } catch (Exception e) {
                Log.e("StockGraphFragment", "Unexpected error", e);
            }

            handler.post(() -> updateChart(entries));
        });
    }

    private void updateChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Stock Data");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new DateAxisValueFormatter());

        chart.setData(lineData);
        chart.invalidate();
    }

    // Custom formatter to convert timestamp to date
    public class DateAxisValueFormatter extends ValueFormatter {
        private final SimpleDateFormat mFormat;

        public DateAxisValueFormatter() {
            // Specify the format you need
            this.mFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            // Convert timestamp to milliseconds and format it
            return mFormat.format(new Date((long) value));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}