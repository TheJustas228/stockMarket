package com.example.stockmarketapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.stockmarketapp.models.StockModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;



public class StockGraphFragment extends Fragment {


    private static final String ARG_STOCK = "stock";
    private StockModel stock;
    private LineChart chart;
    private ExecutorService executorService;
    private Handler handler;
    private String currentInterval = "1h";
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

        // Set the stock symbol at the top
        TextView tvStockSymbol = view.findViewById(R.id.tvStockSymbol);
        tvStockSymbol.setText("Current Stock: " + stock.getSymbol());

        TextView tvStockPrice = view.findViewById(R.id.tvStockPrice);
        tvStockPrice.setText("Latest Price: " + stock.getClosePrice());

        setupButtonListeners(view);
        fetchStockData(stock.getSymbol(), "1h", "1d"); // Default data fetch
        return view;
    }

    private void setupButtonListeners(View view) {
        view.findViewById(R.id.btnOneHour).setOnClickListener(v -> {
            currentInterval = "1h";
            fetchStockData(stock.getSymbol(), "5m", "1h");
        });
        view.findViewById(R.id.btnOneDay).setOnClickListener(v -> {
            currentInterval = "1d";
            fetchStockData(stock.getSymbol(), "1h", "1d");
        });
        view.findViewById(R.id.btnOneHour).setOnClickListener(v -> fetchStockData(stock.getSymbol(), "5m", "1h"));
        view.findViewById(R.id.btnOneDay).setOnClickListener(v -> fetchStockData(stock.getSymbol(), "1h", "1d"));
        view.findViewById(R.id.btnOneWeek).setOnClickListener(v -> fetchStockData(stock.getSymbol(), "1d", "1wk"));
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

        // Disable drawing values on data points
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new DateAxisValueFormatter());
        // Custom MarkerView to show price and date on tap
        CustomMarkerView mv = new CustomMarkerView(getContext(), R.layout.marker_view);
        chart.setMarker(mv);

        chart.invalidate();
    }

    // Custom MarkerView class
    public class CustomMarkerView extends MarkerView {
        private TextView tvContent;
        private SimpleDateFormat dateFormat;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvContent = findViewById(R.id.tvContent);
            updateDateFormat();
        }

        private void updateDateFormat() {
            switch (currentInterval) {
                case "1h":
                    dateFormat = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);
                    break;
                case "1d":
                    dateFormat = new SimpleDateFormat("dd MMM HH:00", Locale.ENGLISH);
                    break;
                default: // For 1wk, 1mo, and 3mo
                    dateFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
                    break;
            }
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            updateDateFormat();
            String date = dateFormat.format(new Date((long) e.getX()));
            String text = "Price: " + e.getY() + "\nDate: " + date;
            tvContent.setText(text);
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
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