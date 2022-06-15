package com.ytu.businesstravelapp;

import static com.ytu.businesstravelapp.MainActivity.firebaseURL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AdminActivity extends AppCompatActivity {
    private RecyclerView rvTrips;
    private AdminTripsAdapter tripsAdapter;
    private ArrayList<Trip> trips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        rvTrips = findViewById(R.id.rvAdminTrips);
        rvTrips.setHasFixedSize(true);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider)));
        rvTrips.addItemDecoration(decoration);

        trips = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseURL);
        DatabaseReference tripRef = database.getReference("trips");

        Log.d("test1",tripRef.toString());
        tripRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trips.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        trip.setId(dataSnapshot.getKey());
                        Log.d("test1",dataSnapshot.getKey());
                        trips.add(trip);
                        Log.d("test1","trip null değil");
                    }
                    else {
                        Log.d("test1","trip null");
                    }
                }
                Log.d("test1",trips.size() + "");

                tripsAdapter = new AdminTripsAdapter(AdminActivity.this, trips);
                rvTrips.setAdapter(tripsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("test1",error.toString());
            }
        });
    }
}