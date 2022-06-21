package com.ytu.businesstravelapp.Activities;

import static com.ytu.businesstravelapp.Activities.MainActivity.firebaseURL;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ytu.businesstravelapp.Classes.Price;
import com.ytu.businesstravelapp.R;

import java.util.ArrayList;

public class PricesActivity extends AppCompatActivity {
    private static final String TAG = "ytu";

    private TextView textView, textView2, textView4, textView5, textView6, textView8, textView9, textView10, textView12;
    private Price blackPrice, bluePrice, yellowPrice;
    private ArrayList<Price> prices;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);

        defineFields();

        Intent intent = getIntent();
        if(intent.hasExtra("user")) { defineSettings(); }
    }

    private void defineSettings() {
        textView.setOnClickListener(view -> createDialog(yellowPrice.getKm(), 1, "km"));
        textView2.setOnClickListener(view -> createDialog(yellowPrice.getOpening(), 1, "opening"));
        textView4.setOnClickListener(view -> createDialog(yellowPrice.getIndibindi(), 1, "indibindi"));
        textView5.setOnClickListener(view -> createDialog(bluePrice.getKm(), 2, "km"));
        textView6.setOnClickListener(view -> createDialog(bluePrice.getOpening(), 2, "opening"));
        textView8.setOnClickListener(view -> createDialog(bluePrice.getIndibindi(), 2, "indibindi"));
        textView9.setOnClickListener(view -> createDialog(blackPrice.getKm(), 3, "km"));
        textView10.setOnClickListener(view -> createDialog(blackPrice.getOpening(), 3, "opening"));
        textView12.setOnClickListener(view -> createDialog(blackPrice.getIndibindi(), 3, "indibindi"));
    }
    private void createDialog(String amount, int type, String child) {
        EditText edittext = new EditText(PricesActivity.this);
        edittext.setText(amount);
        edittext.setGravity(Gravity.CENTER);
        edittext.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        AlertDialog.Builder alert = new AlertDialog.Builder(PricesActivity.this);
        alert.setMessage("Yeni tutarı giriniz");
        alert.setView(edittext);
        alert.setPositiveButton("Onayla", (dialog, whichButton) -> {
            String userInput = edittext.getText().toString();
            if(!userInput.isEmpty()) {
                switch (type) {
                    case 1:
                        database.getReference("prices/yellow").child(child).setValue(edittext.getText().toString());
                        break;
                    case 2:
                        database.getReference("prices/blue").child(child).setValue(edittext.getText().toString());
                        break;
                    case 3:
                        database.getReference("prices/black").child(child).setValue(edittext.getText().toString());
                        break;
                }
            }
        });
        alert.setNegativeButton("İptal", null);

        alert.show();
    }

    private void defineFields() {
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        textView6 = findViewById(R.id.textView6);
        textView8 = findViewById(R.id.textView8);
        textView9 = findViewById(R.id.textView9);
        textView10 = findViewById(R.id.textView10);
        textView12 = findViewById(R.id.textView12);

        prices = new ArrayList<>();
        database = FirebaseDatabase.getInstance(firebaseURL);
        DatabaseReference priceRef = database.getReference("prices");

        priceRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                prices.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Price price= dataSnapshot.getValue(Price.class);

                    if (price != null) { prices.add(price); }
                }

                blackPrice = prices.get(0);
                bluePrice = prices.get(1);
                yellowPrice = prices.get(2);

                textView.setText(yellowPrice.getKm() + " ₺/km");
                textView2.setText("Açılış Ücreti: " + yellowPrice.getOpening() + "₺");
                textView4.setText("İndi Bindi Ücreti: " + yellowPrice.getIndibindi() + "₺");
                textView5.setText(bluePrice.getKm() + " ₺/km");
                textView6.setText("Açılış Ücreti: " + bluePrice.getOpening() + "₺");
                textView8.setText("İndi Bindi Ücreti: " + bluePrice.getIndibindi() + "₺");
                textView9.setText(blackPrice.getKm() + " ₺/km");
                textView10.setText("Açılış Ücreti: " + blackPrice.getOpening() + "₺");
                textView12.setText("İndi Bindi Ücreti: " + blackPrice.getIndibindi() + "₺");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG,error.toString());
            }
        });
    }
}