package com.ytu.businesstravelapp.LocationServices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationUpdatesComponent {
    private static final String TAG = "ytuLog";

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3 * 1000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private com.google.android.gms.location.LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;

    private Location mLocation;

    public ILocationProvider iLocationProvider;

    public LocationUpdatesComponent(ILocationProvider iLocationProvider) {
        this.iLocationProvider = iLocationProvider;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void onCreate(Context context) {
        Log.i(TAG, "created");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.i(TAG, "onCreate onLocationResult loc: " + locationResult.getLastLocation());

                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();
    }

    public void onStart() {
        Log.i(TAG, "onStart");
        requestLocationUpdates();
    }

    public void onStop() {
        Log.i(TAG, "onStop");
        removeLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Could not request updates: " + unlikely);
        }
    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        } catch (SecurityException unlikely) {
            Log.e(TAG, "Could not remove updates:" + unlikely);
        }
    }
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLocation = task.getResult();
                            Log.i(TAG, "getLastLocation " + mLocation);
                            onNewLocation(mLocation);
                        } else {
                            Log.w(TAG, "Failed to get location.");
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        mLocation = location;
        if (this.iLocationProvider != null) {
            this.iLocationProvider.onLocationUpdate(mLocation);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void createLocationRequest() {
        mLocationRequest = new com.google.android.gms.location.LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.QUALITY_HIGH_ACCURACY);
    }

    public interface ILocationProvider {
        void onLocationUpdate(Location location);
    }
}

