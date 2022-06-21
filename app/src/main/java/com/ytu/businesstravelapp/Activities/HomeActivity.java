package com.ytu.businesstravelapp.Activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ytu.businesstravelapp.Fragments.MapFragment;
import com.ytu.businesstravelapp.Fragments.ProfileFragment;
import com.ytu.businesstravelapp.R;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private MapFragment mapFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mapFragment = new MapFragment();
        profileFragment = new ProfileFragment();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.mapButton);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.mapButton) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).commit();
            return true;
        }
        else if (item.getItemId() == R.id.profileButton) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
            return true;
        }
        return false;
    }
}