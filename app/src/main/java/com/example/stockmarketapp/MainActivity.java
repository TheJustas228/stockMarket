package com.example.stockmarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Initialize the Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the FloatingActionButton
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "FAB clicked!", Toast.LENGTH_SHORT).show();
        });

        // Initialize the DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set up the ActionBarDrawerToggle for the DrawerLayout
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the NavigationItemSelectedListener for the NavigationView
        navigationView.setNavigationItemSelectedListener(item -> handleNavigationItemSelected(item));
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }
    private boolean handleNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment selectedFragment = null;

        if (id == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (id == R.id.nav_stock_market) {
            selectedFragment = new StockMarketFragment();
        } else if (id == R.id.nav_news) {
            selectedFragment = new NewsFragment();
        } else if (id == R.id.nav_logout) {
            logoutUser();
            return true; // Return early since there's no fragment change for logout
        }

        if (selectedFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        drawerLayout.closeDrawers();
        return true;
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
