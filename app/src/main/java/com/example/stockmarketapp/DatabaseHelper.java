package com.example.stockmarketapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "stockTrackerDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_STOCKS = "stocks";

    // Stock Table Columns
    private static final String KEY_STOCK_ID = "id";
    private static final String KEY_STOCK_SYMBOL = "symbol";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STOCKS_TABLE = "CREATE TABLE " + TABLE_STOCKS +
                "(" +
                KEY_STOCK_ID + " INTEGER PRIMARY KEY," +
                KEY_STOCK_SYMBOL + " TEXT" +
                ")";

        db.execSQL(CREATE_STOCKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS);
        // Create tables again
        onCreate(db);
    }

    // Insert a new stock symbol into the database
    public void addStock(String symbol) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STOCK_SYMBOL, symbol);

        db.insert(TABLE_STOCKS, null, values);
        db.close();
    }

    // Remove a stock symbol from the database
    public void deleteStock(String symbol) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS, KEY_STOCK_SYMBOL + " = ?", new String[]{symbol});
        db.close();
    }

    // Fetch all tracked stock symbols from the database
    public List<String> getAllStocks() {
        List<String> stocks = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_STOCKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int symbolIndex = cursor.getColumnIndex(KEY_STOCK_SYMBOL);

        if (symbolIndex != -1) {
            if (cursor.moveToFirst()) {
                do {
                    String symbol = cursor.getString(symbolIndex);
                    stocks.add(symbol);
                } while (cursor.moveToNext());
            }
        } else {
            // Log an error or handle the case where the column is not found
            Log.e("DatabaseHelper", "Column " + KEY_STOCK_SYMBOL + " not found in table " + TABLE_STOCKS);
        }

        cursor.close();
        db.close();
        return stocks;
    }
}