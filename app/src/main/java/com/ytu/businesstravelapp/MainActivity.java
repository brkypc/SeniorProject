package com.ytu.businesstravelapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TextView appName;
    LottieAnimationView lottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        appName = findViewById(R.id.appName);
        lottie = findViewById(R.id.lottie);

        appName.animate().translationY(-100).setDuration(500).setStartDelay(0);
        lottie.animate().translationX(1000).setDuration(500).setStartDelay(2000);

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        if(fuser != null) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                startActivity(intent);
                finish();
            }, 500);
        }
        else {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 500);
        }

    }
}