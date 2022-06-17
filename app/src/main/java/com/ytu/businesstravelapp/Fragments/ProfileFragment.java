package com.ytu.businesstravelapp.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ytu.businesstravelapp.Activities.AboutActivity;
import com.ytu.businesstravelapp.Activities.FAQActivity;
import com.ytu.businesstravelapp.Activities.MainActivity;
import com.ytu.businesstravelapp.Activities.PricesActivity;
import com.ytu.businesstravelapp.R;
import com.ytu.businesstravelapp.Activities.TripsActivity;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    public ProfileFragment() { }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
        tripsButton.setOnClickListener(view14 -> {
            Intent intent = new Intent(requireContext(), TripsActivity.class);
            startActivity(intent);
        });

        AppCompatButton pricesButton = view.findViewById(R.id.prices);
        pricesButton.setOnClickListener(view13 -> {
            Intent intent = new Intent(requireContext(), PricesActivity.class);
            startActivity(intent);
        });

        AppCompatButton about = view.findViewById(R.id.about);
        about.setOnClickListener(view12 -> {
            Intent intent = new Intent(requireContext(), AboutActivity.class);
            startActivity(intent);
        });

        AppCompatButton faq = view.findViewById(R.id.faq);
        faq.setOnClickListener(view11 -> {
            Intent intent = new Intent(requireContext(), FAQActivity.class);
            startActivity(intent);
        });

        AppCompatButton logoutButton = view.findViewById(R.id.logout);
        logoutButton.setOnClickListener(view10 -> new AlertDialog.Builder(requireContext())
                .setMessage("Çıkış yapmak istiyor musunuz?")
                .setPositiveButton("Evet", (dialog, whichButton) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Hayır", null).show());

        return view;
    }
}