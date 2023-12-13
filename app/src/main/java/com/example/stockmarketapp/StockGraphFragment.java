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
import android.widget.LinearLayout;

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
import org.json.JSONObject;

import java.io.BufferedReader;
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
        tvStockPrice.setText("Latest Price: " + stock.getClosePrice() + "$");

        // Set up buttons and fetch default data (now using 1h interval)
        setupButtonListeners(view);
        fetchStockData(stock.getSymbol(), "1h", "1d");
        fetchAdditionalStockInfo(view, stock.getSymbol()); // Fixed this line

        return view;
    }
    private void fetchAdditionalStockInfo(View view, String symbol) {
        executorService.execute(() -> {
            String urlString = "https://query1.finance.yahoo.com/v7/finance/options/" + symbol;
            HttpsURLConnection urlConnection = null;

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(result.toString());
                    JSONObject quote = jsonObject.getJSONObject("optionChain")
                            .getJSONArray("result")
                            .getJSONObject(0)
                            .getJSONObject("quote");

                    // Extract the needed information
                    final String marketCap = quote.getString("marketCap");
                    final String previousClose = quote.getString("regularMarketPreviousClose");

                    // Convert dividend date from Unix timestamp to readable date format
                    String dividendDateValue = quote.optString("dividendDate");
                    final String formattedDividendDate;
                    if (dividendDateValue != null && !dividendDateValue.isEmpty()) {
                        long dividendTimestamp = Long.parseLong(dividendDateValue);
                        Date dividendDate = new Date(dividendTimestamp * 1000); // Convert to milliseconds
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                        formattedDividendDate = dateFormat.format(dividendDate);
                    } else {
                        formattedDividendDate = "N/A";
                    }

                    // Extract other values and handle N/A cases similarly
                    final String dividendYield = !quote.isNull("dividendYield") ? quote.getString("dividendYield") + "%" : "N/A";
                    final String bid = !quote.isNull("bid") ? quote.getString("bid") + "$" : "N/A";
                    final String ask = !quote.isNull("ask") ? quote.getString("ask") + "$" : "N/A";
                    final String regularMarketOpen = !quote.isNull("regularMarketOpen") ? quote.getString("regularMarketOpen") + "$" : "N/A";

                    // Update the UI on the main thread
                    handler.post(() -> {
                        TextView tvRegularMarketOpen = view.findViewById(R.id.tvRegularMarketOpen);
                        tvRegularMarketOpen.setText("Market Open: " + regularMarketOpen);

                        TextView tvPreviousClose = view.findViewById(R.id.tvPreviousClose);
                        tvPreviousClose.setText("Previous Close: " + previousClose + "$");

                        TextView tvMarketCap = view.findViewById(R.id.tvMarketCap);
                        tvMarketCap.setText("Market Cap: " + marketCap + "$");

                        TextView tvDividendDate = view.findViewById(R.id.tvDividendDate);
                        tvDividendDate.setText("Previous Dividend Date: " + formattedDividendDate);

                        TextView tvDividendYield = view.findViewById(R.id.tvDividendYield);
                        tvDividendYield.setText("Dividend Yield: " + dividendYield);

                        TextView tvBid = view.findViewById(R.id.tvBid);
                        tvBid.setText("Bid: " + bid);

                        TextView tvAsk = view.findViewById(R.id.tvAsk);
                        tvAsk.setText("Ask: " + ask);


                    });

                }
            } catch (Exception e) {
                Log.e("StockGraphFragment", "Error: " + e.getMessage(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }
    private void setupButtonListeners(View view) {
        // Removed the listener for the 1h button

        // Adjust other button listeners as needed
        view.findViewById(R.id.btnOneDay).setOnClickListener(v -> {
            currentInterval = "1d";
            fetchStockData(stock.getSymbol(), "1h", "1d");
        });
        view.findViewById(R.id.btnOneWeek).setOnClickListener(v -> {
            currentInterval = "1wk";
            fetchStockData(stock.getSymbol(), "1d", "1wk");
        });
        view.findViewById(R.id.btnOneMonth).setOnClickListener(v -> {
            currentInterval = "1mo";
            fetchStockData(stock.getSymbol(), "1d", "1mo");
        });
        view.findViewById(R.id.btnThreeMonths).setOnClickListener(v -> {
            currentInterval = "3mo";
            fetchStockData(stock.getSymbol(), "1wk", "3mo");
        });
    }

    private void fetchStockData(String symbol, String interval, String range) {
        String urlString = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=" + interval + "&range=" + range;

        executorService.execute(() -> {
            List<Entry> entries = new ArrayList<>();
            HttpsURLConnection urlConnection = null;

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Log the entire JSON response
                    Log.d("StockGraphFragment", "JSON Response: " + result.toString());

                    JSONObject jsonObject = new JSONObject(result.toString());
                    if (jsonObject.has("chart")) {
                        JSONObject chartObject = jsonObject.getJSONObject("chart");
                        JSONArray resultArray = chartObject.getJSONArray("result");
                        JSONObject firstResult = resultArray.getJSONObject(0);
                        JSONObject indicators = firstResult.getJSONObject("indicators");

                        if (indicators.has("quote")) {
                            JSONObject quoteObject = indicators.getJSONArray("quote").getJSONObject(0);

                            if (quoteObject.has("close")) {
                                JSONArray closeArray = quoteObject.getJSONArray("close");
                                JSONArray timestampArray = firstResult.getJSONArray("timestamp");

                                for (int i = 0; i < closeArray.length(); i++) {
                                    if (!closeArray.isNull(i)) {
                                        float closeValue = (float) closeArray.getDouble(i);
                                        long timestamp = timestampArray.getLong(i) * 1000;
                                        entries.add(new Entry(timestamp, closeValue));
                                    }
                                }
                            } else {
                                Log.e("StockGraphFragment", "Close data not available for this interval");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("StockGraphFragment", "Error: " + e.getMessage(), e);
            } finally {
                handler.post(() -> updateChart(entries));
            }
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