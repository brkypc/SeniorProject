package com.ytu.businesstravelapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ytu.businesstravelapp.PricesActivity;
import com.ytu.businesstravelapp.R;
import com.ytu.businesstravelapp.TripsActivity;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        AppCompatButton tripsButton = view.findViewById(R.id.prevTrips);
        tripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), TripsActivity.class);
                startActivity(intent);
            }
        });

        AppCompatButton pricesButton = view.findViewById(R.id.prices);
        pricesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), PricesActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}