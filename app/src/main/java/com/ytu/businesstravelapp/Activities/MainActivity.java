package com.ytu.businesstravelapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ytu.businesstravelapp.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static final String firebaseURL = "https://businesstravel-352310-default-rtdb.europe-west1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        if(fUser != null) {
            if (Objects.requireNonNull(fUser.getEmail()).equalsIgnoreCase("admin@ytu.com")) {
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(intent);
                    finish();
                }, 3750);
            }
            else {
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }, 3750);
            }
        }
        else {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 3750);
        }

    }
}