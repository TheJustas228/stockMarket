package com.example.stockmarketapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.AsyncTask;
import android.graphics.Color;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.example.stockmarketapp.models.StockModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class StockGraphFragment extends Fragment {

    private static final String ARG_STOCK = "stock";
    private StockModel stock; // Field to hold the stock model
    private LineChart chart;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_graph, container, false);
        chart = view.findViewById(R.id.chart);
        new FetchStockDataTask().execute(stock.getSymbol());
        return view;
    }

    private class FetchStockDataTask extends AsyncTask<String, Void, List<Entry>> {
        @Override
        protected List<Entry> doInBackground(String... params) {
            List<Entry> entries = new ArrayList<>();
            try {
                URL url = new URL("https://query1.finance.yahoo.com/v8/finance/chart/" + params[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
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
                    JSONArray closeArray = indicators.getJSONArray("quote").getJSONObject(0).getJSONArray("close");
                    JSONArray timestampArray = firstResult.getJSONArray("timestamp");

                    for (int i = 0; i < closeArray.length(); i++) {
                        if (!closeArray.isNull(i)) {
                            float closeValue = (float) closeArray.getDouble(i);
                            long timestamp = timestampArray.getLong(i);
                            entries.add(new Entry(timestamp, closeValue));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return entries;
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            LineDataSet dataSet = new LineDataSet(entries, "Stock Data");
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
        }
    }
    // Additional methods for the fragment can be added here
}
