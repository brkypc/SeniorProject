package com.ytu.businesstravelapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ytu.businesstravelapp.LoginActivity;
import com.ytu.businesstravelapp.PricesActivity;
import com.ytu.businesstravelapp.R;
import com.ytu.businesstravelapp.TripsActivity;

import java.util.Arrays;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        TextView name = view.findViewById(R.id.name);
        TextView email = view.findViewById(R.id.email);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser!= null) {
            String uEmail = fUser.getEmail();
            String[] uName = Objects.requireNonNull(uEmail).split("@");
            uName = uName[0].split("\\.");
            if(uName.length > 1) {
                String fName1 = uName[0].substring(0, 1).toUpperCase() + uName[0].substring(1);
                String fName2 = uName[1].substring(0, 1).toUpperCase() + uName[1].substring(1);
                name.setText(fName1 + " " + fName2);
            }
            else {
                name.setText(uName[0].substring(0, 1).toUpperCase() + uName[0].substring(1));
            }

            email.setText(uEmail);
        }


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

        AppCompatButton logoutButton = view.findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth firebaseAuth  = FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        return view;
    }
}