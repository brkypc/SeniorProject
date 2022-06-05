package com.ytu.businesstravelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ytu.businesstravelapp.Fragments.MapFragment;
import com.ytu.businesstravelapp.Fragments.ProfileFragment;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    //MAPS_API_KEY=AIzaSyAaflO4djVC3VTRXf9SpyXF16U1i0LDzK4
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.profileButton);

    }
    MapFragment mapFragment = new MapFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mapButton:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).commit();
                return true;

            case R.id.profileButton:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
                return true;
        }
        return false;
    }
}