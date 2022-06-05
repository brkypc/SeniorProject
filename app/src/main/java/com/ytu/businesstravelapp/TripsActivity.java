package com.ytu.businesstravelapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Objects;

public class TripsActivity extends AppCompatActivity {

    private RecyclerView rvTrips;
    private TripsAdapter tripsAdapter;
    private ArrayList<Trip> trips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        rvTrips = findViewById(R.id.rvTrips);
        rvTrips.setHasFixedSize(true);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider)));
        rvTrips.addItemDecoration(decoration);

        trips = new ArrayList<>();
        trips.add(new Trip("12 Haziran 2022 15.56","2 s 10 dk","1","55.45 km","987₺"));
        trips.add(new Trip("13 Haziran 2022 15.56","3 s 10 dk","2","43.45 km","345₺"));
        trips.add(new Trip("15 Haziran 2022 15.56","4 s 10 dk","1","23.45 km","28₺"));
        trips.add(new Trip("16 Haziran 2022 15.56","1 s 10 dk","3","33.45 km","536₺"));
        trips.add(new Trip("17 Haziran 2022 15.56","2 s 10 dk","2","23 km","1555₺"));
        trips.add(new Trip("18 Haziran 2022 15.56","50 dk","3","10 km","123₺"));
        tripsAdapter = new TripsAdapter(this, trips);
        rvTrips.setAdapter(tripsAdapter);
    }
}