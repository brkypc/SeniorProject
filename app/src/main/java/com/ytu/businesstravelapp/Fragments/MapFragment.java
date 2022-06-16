package com.ytu.businesstravelapp.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ytu.businesstravelapp.LocationServices.MyIntentService;
import com.ytu.businesstravelapp.R;
import com.ytu.businesstravelapp.Classes.Taxi;
import com.ytu.businesstravelapp.Adapters.TaxiAdapter;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private final static int LOCATION_REQUEST_CODE = 45;
    private GoogleMap mMap;
    private LatLng mLatLng;
    private String address;
    public static final String MESSENGER_INTENT_KEY = "msg-intent-key";
    private IncomingMessageHandler mHandler;
    private static ArrayList<Location> locations;
    private RecyclerView rvTaxis;
    private TaxiAdapter taxiAdapter;
    private ArrayList<Taxi> taxis;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        rvTaxis = view.findViewById(R.id.rvTaxis);
        rvTaxis.setHasFixedSize(true);
        SnapHelper snapHelper = new PagerSnapHelper();
        rvTaxis.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        snapHelper.attachToRecyclerView(rvTaxis);

        taxis = new ArrayList<>();
        taxis.add(new Taxi("1", "9.80", "6.30", "28"));
        taxis.add(new Taxi("2", "11.27", "7.25", "32.20"));
        taxis.add(new Taxi("3", "16.66", "10.71", "47.60"));
        taxiAdapter = new TaxiAdapter(requireContext(), taxis);
        rvTaxis.setAdapter(taxiAdapter);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ImageView callTaxi = view.findViewById(R.id.callTaxi);
        callTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + "com.bitaksi.musteri"));
                requireActivity().startActivity(intent);
            }
        });


        @SuppressLint("DefaultLocale")
        String s1 = String.format("%.1f", 2.3434343);
        Log.d("test", s1);
        s1 = s1.replace(',', '.');
        Log.d("test", Float.valueOf(s1) + "");

        float calculatedReceipt2 = (float) (Float.parseFloat(s1)*(6.3) + 9.8);
        Log.d("test",calculatedReceipt2 + "");

        locations = new ArrayList<>();
        mHandler = new IncomingMessageHandler();
        /*ToggleButton startButton = view.findViewById(R.id.btnStart);

        startButton.setOnClickListener(v -> {
            if(!startButton.isChecked()) {
                requireContext().stopService(new Intent(getContext(), MyIntentService.class));
                Log.d("test", "stopped service" );
                for (Location l:locations) {
                    Log.d("test", l.getLatitude() + " long:" + l.getLongitude());
                }
                float result = locations.get(0).distanceTo(locations.get(locations.size()-1));
                Log.d("test", String.valueOf(result/1000));
                Log.d("test", String.valueOf(result));

                @SuppressLint("DefaultLocale")
                String s = String.format("%.1f", result/1000);
                s = s.replace(',', '.');
                float calculatedReceipt = (float) (Float.parseFloat(s)*(6.3) + 9.8);
                if (calculatedReceipt<28) calculatedReceipt= 28.0F;
                Log.d("test",calculatedReceipt + "");


                Intent photo = new Intent(getContext(), PhotoActivity.class);
                photo.putExtra("amount", String.valueOf(calculatedReceipt));
                photo.putExtra("km", s);
                startActivity(photo);

            }
            else {
                Intent startServiceIntent = new Intent(getContext(), MyIntentService.class);
                Messenger messengerIncoming = new Messenger(mHandler);
                startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
                requireContext().startService(startServiceIntent);
                Log.d("test", "started service");
            }

        });*/

        return view;
    }

    public static class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("test", "handleMessage..." + msg.toString());

            super.handleMessage(msg);

            if (msg.what == MyIntentService.LOCATION_MESSAGE) {

                Location obj = (Location) msg.obj;
                locations.add(obj);
                float[] results = new float[10];
                android.location.Location.distanceBetween(locations.get(0).getLatitude(), locations.get(0).getLongitude(),
                        locations.get(locations.size() - 1).getLatitude(), locations.get(locations.size() - 1).getLongitude(), results);

                for (float f : results) {
                    Log.d("test", String.valueOf(f));
                }
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                //locationMsg.setText("LAT :  " + obj.getLatitude() + "\nLNG : " + obj.getLongitude() + "\n\n" + obj.toString() + " \n\n\nLast updated- " + currentDateTimeString);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }

        LatLng lng = new LatLng(40.99055195081905, 29.02918425499755);
        CameraUpdate starting = CameraUpdateFactory.newLatLngZoom(
                lng, 10);
        mMap.animateCamera(starting);

        mMap.setOnMapLongClickListener(latLng -> {
            mLatLng = latLng;
            mMap.clear();

            address = getAddress(latLng);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(address);

            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, 15);

            mMap.animateCamera(location);
            mMap.addMarker(markerOptions);
        });

        mMap.setOnMarkerClickListener(marker -> {
            return false;
        });
    }

    private String getAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(requireContext());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            return addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return true;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           requireActivity().recreate();
        } else {
            Toast.makeText(requireContext(), "İzin verilmedi", Toast.LENGTH_SHORT).show();
        }
    }
}