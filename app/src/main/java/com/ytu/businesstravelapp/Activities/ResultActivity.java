package com.ytu.businesstravelapp.Activities;

import static com.ytu.businesstravelapp.Activities.MainActivity.firebaseURL;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.ytu.businesstravelapp.R;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "ytuLog";

    private String distance, date, tripTime, taxiType, calculatedPrice, ocrResult, oLat, oLong, dLat, dLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        distance = intent.getStringExtra("distance");
        date = intent.getStringExtra("date");
        tripTime = intent.getStringExtra("tripTime");
        taxiType = intent.getStringExtra("taxiType");
        calculatedPrice = intent.getStringExtra("amount");
        ocrResult = intent.getStringExtra("ocr");
        oLat = intent.getStringExtra("oLat");
        oLong = intent.getStringExtra("oLong");
        dLat = intent.getStringExtra("dLat");
        dLong = intent.getStringExtra("dLong");

        Log.d(TAG, oLat);
        Log.d(TAG, oLong);
        Log.d(TAG, dLat);
        Log.d(TAG, dLong);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.reportMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        TextView amountField = findViewById(R.id.amount);
        TextView distanceField = findViewById(R.id.distance);
        TextView ocrField = findViewById(R.id.ocrAmount);
        AppCompatButton saveDatabase = findViewById(R.id.saveDatabase);

        distanceField.setText(MessageFormat.format("{0} km", distance));
        amountField.setText(MessageFormat.format("{0} ₺", calculatedPrice));
        ocrField.setText(MessageFormat.format("{0} ₺", ocrResult));

        saveDatabase.setOnClickListener(view -> saveToFirebase(ocrResult));
    }


    private void saveToFirebase(String amount) {
        String status = "no";

        if (Float.parseFloat(amount) < Float.parseFloat(calculatedPrice)) {
            Log.d(TAG, "bill price equals calculated price ");
            status = "yes";
        } else if (Float.parseFloat(amount) < (Float.parseFloat(calculatedPrice) * 1.1)) { //%10 error rate
            Log.d(TAG, "bill price lesser then 1.1*calculated price");
            status = "yes";
        }
        Log.d(TAG, "Bill Price: " + Float.parseFloat(amount));
        Log.d(TAG, "Calculated Price: " + Float.parseFloat(calculatedPrice));
        Log.d(TAG, "1.1*Calculated Price: " + Float.parseFloat(calculatedPrice) * 1.1);

        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseURL);
        DatabaseReference tripRef = database.getReference("trips");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("date", date);
        hashMap.put("tripTime", tripTime);
        hashMap.put("taxiType", taxiType);
        hashMap.put("distance", distance);
        hashMap.put("amount", calculatedPrice);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        String[] uName;
        if (fUser != null) {
            String uEmail = fUser.getEmail();
            uName = Objects.requireNonNull(uEmail).split("@");
            uName = uName[0].split("\\.");
            if (uName.length > 1) {
                String fName1 = uName[0].substring(0, 1).toUpperCase() + uName[0].substring(1);
                String fName2 = uName[1].substring(0, 1).toUpperCase() + uName[1].substring(1);
                hashMap.put("nameSurname", fName1 + " " + fName2);
            } else {
                hashMap.put("nameSurname", uName[0].substring(0, 1).toUpperCase() + uName[0].substring(1));
            }
        }
        hashMap.put("billPrice", amount);
        hashMap.put("status", status);

        tripRef.push().setValue(hashMap);
        Toast.makeText(ResultActivity.this, "Seyahatiniz kaydedildi", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ResultActivity.this, TripsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng origin = new LatLng(Double.parseDouble(oLat), Double.parseDouble(oLong));
        LatLng destination = new LatLng(Double.parseDouble(dLat), Double.parseDouble(dLong));

        googleMap.addMarker(new MarkerOptions().position(origin).title("Başlangıç"));
        googleMap.addMarker(new MarkerOptions().position(destination).title("Bitiş"));
        ArrayList<LatLng> path = getDirections(origin, destination);

        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            googleMap.addPolyline(opts);
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));
    }

    private ArrayList<LatLng> getDirections(LatLng origin, LatLng destination) {
        ArrayList<LatLng> path = new ArrayList<>();
        Log.d(TAG, origin.latitude + "," + origin.longitude + "\n" + destination.latitude + "," + destination.longitude);

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("api_key")
                .build();
        DirectionsApiRequest req =
                DirectionsApi.getDirections(context, origin.latitude + "," + origin.longitude,
                        destination.latitude + "," + destination.longitude);
        try {
            DirectionsResult res = req.await();

            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        return path;
    }
}
