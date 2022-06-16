package com.ytu.businesstravelapp.Activities;

import static com.ytu.businesstravelapp.Activities.MainActivity.firebaseURL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ytu.businesstravelapp.R;
import com.ytu.businesstravelapp.Classes.Trip;
import com.ytu.businesstravelapp.Adapters.TripsAdapter;

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
        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseURL);
        DatabaseReference tripRef = database.getReference("trips");

        /*HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("date","4 Haziran 2022 14:22");
        hashMap.put("tripTime", "23 dk");
        hashMap.put("taxiType", "1");
        hashMap.put("distance", "6.60");
        hashMap.put("amount", "51.38");

        tripRef.push().setValue(hashMap);*/

        Log.d("test1",tripRef.toString());
        tripRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trips.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Trip trip = dataSnapshot.getValue(Trip.class);

                    if (trip != null) {
                        trips.add(trip);
                        Log.d("test1","trip null değil");
                    }
                    else {
                        Log.d("test1","trip null");
                    }
                }
                Log.d("test1",trips.size() + "");

                tripsAdapter = new TripsAdapter(TripsActivity.this, trips);
                rvTrips.setAdapter(tripsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("test1",error.toString());
            }
        });

    }
}