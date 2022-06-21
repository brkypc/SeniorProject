package com.ytu.businesstravelapp.Fragments;

import static com.ytu.businesstravelapp.Activities.MainActivity.firebaseURL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.ytu.businesstravelapp.Adapters.TaxiAdapter;
import com.ytu.businesstravelapp.Classes.Price;
import com.ytu.businesstravelapp.Classes.Taxi;
import com.ytu.businesstravelapp.Geofence.GeofenceHelper;
import com.ytu.businesstravelapp.LocationServices.MyIntentService;
import com.ytu.businesstravelapp.R;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private static final String TAG = "ytuLog";
    private static final String GEOFENCE_ID = "MY_GEOFENCE_ID";
    public static final String MESSENGER_INTENT_KEY = "msg-intent-key";
    public static final String MESSENGER_INTENT_KEY2 = "msg-intent-key2";

    private final int FINE_LOCATION_REQUEST_CODE = 15476;
    private final int BACKGROUND_LOCATION_REQUEST_CODE = 18734;
    private static final float GEOFENCE_RADIUS = 2000;

    private static GoogleMap mMap;
    private static ArrayList<Location> locations;
    private static Polyline gpsTrack;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private IncomingMessageHandler mHandler;
    private RecyclerView rvTaxis;
    private TaxiAdapter taxiAdapter;
    private ArrayList<Taxi> taxis;
    private ArrayList<Price> prices;
    private Price blackPrice, bluePrice, yellowPrice;
    private LatLng geoLocation;

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        geoLocation = new LatLng(41.0415073080969, 28.987116142021748);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        if (mapFragment != null) { mapFragment.getMapAsync(this); }

        geofencingClient = LocationServices.getGeofencingClient(requireContext());
        geofenceHelper = new GeofenceHelper(requireContext());

        rvTaxis = view.findViewById(R.id.rvTaxis);
        rvTaxis.setHasFixedSize(true);
        SnapHelper snapHelper = new PagerSnapHelper();
        rvTaxis.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        snapHelper.attachToRecyclerView(rvTaxis);

        taxis = new ArrayList<>();
        prices = new ArrayList<>();
        locations = new ArrayList<>();
        mHandler = new IncomingMessageHandler();

        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseURL);
        DatabaseReference priceRef = database.getReference("prices");

        priceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                prices.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Price price = dataSnapshot.getValue(Price.class);
                    if (price != null) { prices.add(price); }
                }
                blackPrice = prices.get(0);
                bluePrice = prices.get(1);
                yellowPrice = prices.get(2);

                taxis.add(new Taxi("1", yellowPrice.getOpening(), yellowPrice.getKm(), yellowPrice.getIndibindi()));
                taxis.add(new Taxi("2", bluePrice.getOpening(), bluePrice.getKm(), bluePrice.getIndibindi()));
                taxis.add(new Taxi("3", blackPrice.getOpening(), blackPrice.getKm(), blackPrice.getIndibindi()));

                taxiAdapter = new TaxiAdapter(requireContext(), taxis, mHandler);
                rvTaxis.setAdapter(taxiAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.toString());
            }
        });

        ImageView callTaxi = view.findViewById(R.id.callTaxi);
        callTaxi.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + "com.bitaksi.musteri"));
            requireActivity().startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (mMap != null)
//            //mMap.clear();
    }

    public static class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("test", "handleMessage..." + msg.toString());

            super.handleMessage(msg);

            if (msg.what == MyIntentService.LOCATION_MESSAGE) {
                if (!msg.obj.equals("finished")) {
                    Location obj = (Location) msg.obj;
                    locations.add(obj);
                    LatLng lastKnownLatLng = new LatLng(obj.getLatitude(), obj.getLongitude());
                    List<LatLng> points = gpsTrack.getPoints();
                    points.add(lastKnownLatLng);
                    //gpsTrack.setPoints(points);
                    if(locations.size()>1) {
                        LatLng origin = new LatLng(locations.get(0).getLatitude(), locations.get(0).getLongitude());
                        LatLng destination = new LatLng(locations.get(locations.size() - 1).getLatitude(), locations.get(locations.size() - 1).getLongitude());
                        mMap.addMarker(new MarkerOptions().position(origin).title("Başlangıç"));
                        mMap.addMarker(new MarkerOptions().position(destination).title("Bitiş"));
                        ArrayList<LatLng> path = getDirections(origin, destination);

                        if (path.size() > 0) {
                            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
                            mMap.addPolyline(opts);
                        }

                        mMap.getUiSettings().setZoomControlsEnabled(true);

                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));
                    }

                } else {
                    Log.d(TAG, "directions");
                    Log.d(TAG, "size:" + locations.size());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(4);
        gpsTrack = mMap.addPolyline(polylineOptions);

        LatLng lng = new LatLng(40.99055195081905, 29.02918425499755);
        CameraUpdate starting = CameraUpdateFactory.newLatLngZoom(
                lng, 10);
        mMap.animateCamera(starting);

        createGeofence();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void createGeofence() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        BACKGROUND_LOCATION_REQUEST_CODE);
            } else {
                mMap.clear();
                addMarker(geoLocation);
                addCircle(geoLocation);
                addGeofence(geoLocation);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, GEOFENCE_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);

        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> Log.d("test2", "onSuccess: Geofence Added..."))
                .addOnFailureListener(e -> Log.d("test2", "onFailure: " + e.getMessage()));
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Hotel");
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(GEOFENCE_RADIUS);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }


    @Override
    public boolean onMyLocationButtonClick() {
        return true;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    private static ArrayList<LatLng> getDirections(LatLng origin, LatLng destination) {
        //Define list to get all latlng for the route
        ArrayList<LatLng> path = new ArrayList<>();
        Log.d("test", origin.latitude + "," + origin.longitude + "\n" + destination.latitude + "," + destination.longitude);

        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyAaflO4djVC3VTRXf9SpyXF16U1i0LDzK4")
                .build();
        DirectionsApiRequest req =
                DirectionsApi.getDirections(context, origin.latitude + "," + origin.longitude,
                        destination.latitude + "," + destination.longitude);
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
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
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
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
            Log.e("test1", ex.getLocalizedMessage());
        }
        return path;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "İzin verildi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "İzin verilmedi", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == BACKGROUND_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "İzin verildi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Uygulamayı kullanabilmek için arkaplan konum izni verilmelidir.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}