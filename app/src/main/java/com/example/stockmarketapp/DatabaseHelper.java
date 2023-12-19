package com.example.stockmarketapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private DatabaseReference databaseReference;
    private static final String USERS_NODE = "users";
    private static final String STOCKS_NODE = "stocks";

    public DatabaseHelper() {
        // Initialize Firebase Database reference
        this.databaseReference = FirebaseDatabase.getInstance().getReference(USERS_NODE);
    }

    public DatabaseReference getUserStocksReference() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return databaseReference.child(userId).child(STOCKS_NODE);
    }
    public void addStock(String symbol) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.child("userStocks").child(userId).child(symbol).setValue(true);
    }

    public void deleteStock(String symbol) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.child("userStocks").child(userId).child(symbol).removeValue();
    }

    public List<String> getAllStocks() {
        // Placeholder implementation. Replace with actual logic to fetch data from Firebase.
        return new ArrayList<>();
    }
}