package com.ytu.businesstravelapp.Activities;

import static com.ytu.businesstravelapp.Activities.MainActivity.firebaseURL;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ytu.businesstravelapp.Adapters.AdminTripsAdapter;
import com.ytu.businesstravelapp.Classes.Trip;
import com.ytu.businesstravelapp.R;

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

        ImageView logout = findViewById(R.id.adminLogout);
        logout.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setMessage("Çıkış yapmak istiyor musunuz?")
                .setPositiveButton("Evet", (dialog, whichButton) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hayır", null).show());

        ImageView settings = findViewById(R.id.adminSettings);
        settings.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, PricesActivity.class);
            intent.putExtra("user", "admin");
            startActivity(intent);
        });

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