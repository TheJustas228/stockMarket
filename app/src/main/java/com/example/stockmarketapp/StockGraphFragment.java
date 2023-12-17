package com.example.stockmarketapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

    private SharedViewModel viewModel;
    private StockModel currentStock; // The stock that this fragment is displaying

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
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        executorService = Executors.newSingleThreadExecutor(); // Initialize the ExecutorService here
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_graph, container, false);
        chart = view.findViewById(R.id.chart);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        // Set the stock symbol at the top
        TextView tvStockSymbol = view.findViewById(R.id.tvStockSymbol);
        tvStockSymbol.setText(stock.getSymbol());

        TextView tvStockPrice = view.findViewById(R.id.tvStockPrice);
        tvStockPrice.setText("Latest Price: " + stock.getClosePrice() + "$");

        // Set up buttons and fetch default data (now using 1h interval)
        setupButtonListeners(view);
        fetchStockData(stock.getSymbol(), "1h", "1d");
        fetchAdditionalStockInfo(view, stock.getSymbol()); // Fixed this line

        Button trackStockButton = view.findViewById(R.id.btnTrackStock);
        trackStockButton.setOnClickListener(v -> trackStock(stock)); // Pass 'stock' as an argument

        return view;
    }

    private void trackStock(StockModel stock) {
        if (stock != null) {
            fetchAdditionalStockInfo(null, stock.getSymbol()); // Fetch additional stock info
            DatabaseHelper db = new DatabaseHelper(getContext());

            if (!viewModel.isStockTracked(stock.getSymbol())) {
                db.addStock(stock.getSymbol());
                Log.d("StockGraphFragment", "Stock tracked: " + stock.getSymbol());
                viewModel.trackStock(stock); // Track the stock if it's not already tracked
            } else {
                Log.d("StockGraphFragment", "Stock already tracked: " + stock.getSymbol());
            }
        }
    }

    public interface OnStockTrackedListener {
        void onStockTracked(StockModel stock);
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
                    double change = 0.0;

                    // Handle potential JSONException for 'change'
                    try {
                        change = quote.getDouble("regularMarketChange");
                    } catch (JSONException e) {
                        Log.e("StockGraphFragment", "Error parsing 'regularMarketChange': " + e.getMessage());
                    }

                    // Handle dividend date conversion
                    final String formattedDividendDate;
                    String formattedDividendDate1;
                    try {
                        long dividendDateTimestamp = quote.getLong("dividendDate");
                        Date dividendDate = new Date(dividendDateTimestamp * 1000); // Convert to milliseconds
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                        formattedDividendDate1 = dateFormat.format(dividendDate);
                    } catch (JSONException e) {
                        formattedDividendDate1 = "N/A";
                        Log.e("StockGraphFragment", "Error parsing 'dividendDate': " + e.getMessage());
                    }

                    // Extract other values
                    formattedDividendDate = formattedDividendDate1;
                    final String dividendYield = !quote.isNull("dividendYield") ? quote.getString("dividendYield") + "%" : "N/A";
                    final String bid = !quote.isNull("bid") ? quote.getString("bid") + "$" : "N/A";
                    final String ask = !quote.isNull("ask") ? quote.getString("ask") + "$" : "N/A";
                    final String regularMarketOpen = !quote.isNull("regularMarketOpen") ? quote.getString("regularMarketOpen") + "$" : "N/A";

                    // Update the UI on the main thread
                    double finalChange = change;
                    handler.post(() -> {
                        if(isAdded() && view != null) {
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

                            // Update the stock model with the fetched data
                            stock.setClosePrice(Double.parseDouble(previousClose));
                            stock.setChange(finalChange);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("StockGraphFragment", "Error fetching stock info: " + e.getMessage(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    private void setupButtonListeners(View view) {
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
            HttpsURLConnection urlConnection = null;

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                // Process the JSON response
                JSONObject jsonObject = new JSONObject(result.toString());
                List<Entry> entries = parseChartData(jsonObject);
                handler.post(() -> updateChart(entries));

            } catch (Exception e) {
                Log.e("StockGraphFragment", "Error fetching stock data: " + e.getMessage(), e);
                handler.post(() -> {
                    // Handle UI updates or error messages here
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    private List<Entry> parseChartData(JSONObject jsonObject) throws JSONException {
        List<Entry> entries = new ArrayList<>();
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
                            long timestamp = timestampArray.getLong(i) * 1000; // Convert to milliseconds
                            entries.add(new Entry(timestamp, closeValue));
                        }
                    }
                }
            }
        }
        return entries;
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
            executorService.shutdown(); // Shutdown the ExecutorService when the Fragment is destroyed
        }
    }
}