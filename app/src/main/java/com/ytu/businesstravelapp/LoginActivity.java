package com.ytu.businesstravelapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout email, password;
    AppCompatButton loginButton;
    ProgressBar progressBar;
    ImageView info;


    String userEnteredEmail, userEnteredPassword;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        defineVariables();
        defineListeners();
    }

    private void validateUser() {
        auth.signInWithEmailAndPassword(userEnteredEmail, userEnteredPassword)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        progressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, "Giriş Yaptınız.", Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        },1000);
                    }
                    else {
                        loginButton.setClickable(true);
                        Toast.makeText(LoginActivity.this, "Bilgileriniz Yanlış!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean checkFields() {
        boolean validate = true;

        userEnteredEmail = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
        userEnteredPassword = Objects.requireNonNull(password.getEditText()).getText().toString().trim();

        if(userEnteredEmail.isEmpty()) {
            email.setError("Kardeşim e-postayı unuttun");
            validate = false;
        }
        else {
            email.setError(null);
        }

        if(userEnteredPassword.isEmpty()) {
            password.setError("Kardeşim şifreyi unuttun");
            validate = false;
        }
        else {
            password.setError(null);
        }

        return validate;
    }

    private void defineListeners() {
        loginButton.setOnClickListener(v -> {
            if(checkFields()) {
                loginButton.setClickable(false);
                validateUser();
            }
        });
    }

    private void defineVariables() {
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
    }
}