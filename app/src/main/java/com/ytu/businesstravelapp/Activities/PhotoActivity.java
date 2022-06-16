package com.ytu.businesstravelapp.Activities;

import static com.ytu.businesstravelapp.Activities.MainActivity.firebaseURL;

import static java.lang.Math.max;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ytu.businesstravelapp.OCR.CameraSettings.BitmapUtils;
import com.ytu.businesstravelapp.OCR.CameraSettings.GraphicOverlay;
import com.ytu.businesstravelapp.R;
import com.ytu.businesstravelapp.OCR.CameraSettings.VisionImageProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Objects;

public class PhotoActivity extends AppCompatActivity {
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private String distance, date, tripTime, taxiType, calculatedPrice, ocrResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

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

        saveDatabase.setOnClickListener(view -> {
            saveToFirebase(ocrResult);
        });
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
        Toast.makeText(PhotoActivity.this, "Seyahatiniz kaydedildi", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PhotoActivity.this, TripsActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //chooseImage(PhotoActivity.this);
            } else {
                Toast.makeText(this, "İzin reddedildi.", Toast.LENGTH_LONG).show();
            }
        }
    }

}