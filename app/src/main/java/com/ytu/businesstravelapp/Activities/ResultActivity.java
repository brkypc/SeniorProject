package com.ytu.businesstravelapp.Activities;

import static com.ytu.businesstravelapp.Activities.MainActivity.firebaseURL;

import static java.lang.Math.max;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ytu.businesstravelapp.R;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Objects;

public class ResultActivity extends AppCompatActivity {
    private String distance, date, tripTime, taxiType, calculatedPrice, ocrResult;

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

        if (Float.parseFloat(amount) < Float.parseFloat(amount)) {
            Log.d("test1", "fatura küçüktür hesaplanan");
            status = "yes";
        } else if (Float.parseFloat(amount) < (Float.parseFloat(amount) * 1.2)) {
            Log.d("test1", "fatura küçüktür 1.2 hesaplanan");
            status = "yes";
        }
        Log.d("test1", "" + Float.parseFloat(amount));
        Log.d("test1", "" + Float.parseFloat(calculatedPrice) * 1.2);

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

        //tripRef.push().setValue(hashMap);
        Toast.makeText(ResultActivity.this, "Seyahatiniz kaydedildi", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ResultActivity.this, TripsActivity.class);
        startActivity(intent);
        finish();
    }
}