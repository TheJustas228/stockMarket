package com.example.stockmarketapp;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private DatabaseReference databaseReference;

    public DatabaseHelper() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            // Set the database URL explicitly
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://stock-market-app-c39d9-default-rtdb.europe-west1.firebasedatabase.app/");
            databaseReference = database.getReference("userStocks").child(userId);
        } else {
            throw new IllegalStateException("User must be logged in to use DatabaseHelper");
        }
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public void addStock(String symbol) {
        if (databaseReference != null) {
            databaseReference.child(symbol).setValue(true)
                    .addOnSuccessListener(aVoid -> Log.d("DatabaseHelper", "Stock successfully added: " + symbol))
                    .addOnFailureListener(e -> Log.e("DatabaseHelper", "Error adding stock: " + symbol, e));
        } else {
            Log.e("DatabaseHelper", "DatabaseReference is null");
        }
    }

    public void deleteStock(String symbol) {
        if (databaseReference != null) {
            Log.d("DatabaseHelper", "Deleting stock: " + symbol);
            databaseReference.child(symbol).removeValue();
        } else {
            Log.e("DatabaseHelper", "DatabaseReference is null");
        }
    }

    public List<String> getAllStocks() {
        // Placeholder implementation. Replace with actual logic to fetch data from Firebase.
        return new ArrayList<>();
    }
}