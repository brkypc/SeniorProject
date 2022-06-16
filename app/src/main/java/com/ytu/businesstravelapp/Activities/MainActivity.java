package com.ytu.businesstravelapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ytu.businesstravelapp.R;

public class MainActivity extends AppCompatActivity {
    public static final String firebaseURL = "https://businesstravel-352310-default-rtdb.europe-west1.firebasedatabase.app/";
    private TextView appName;
    private LottieAnimationView lottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        appName = findViewById(R.id.appName);
        lottie = findViewById(R.id.lottie);

        //appName.animate().translationY(300).setDuration(500).setStartDelay(0);
        //lottie.animate().translationX(1000).setDuration(500).setStartDelay(2000);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("mySharedPref", MODE_PRIVATE);
            if(sharedPreferences.getString("userType", "").equalsIgnoreCase("admin")) {
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(intent);
                    finish();
                }, 500);
            }
            else {
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }, 500);
            }
        }
        else {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 500); // real 3750
        }

    }
}