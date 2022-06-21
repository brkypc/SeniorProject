package com.ytu.businesstravelapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.ytu.businesstravelapp.R;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout email, password;
    private AppCompatButton loginButton;
    private ProgressBar progressBar;
    private TextView resetPassword;

    private String userEnteredEmail, userEnteredPassword;
    private FirebaseAuth auth;

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
                        Toast.makeText(LoginActivity.this, "Giriş Başarılı", Toast.LENGTH_SHORT).show();

                        SharedPreferences sharedPreferences = getSharedPreferences("mySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        if(userEnteredEmail.equalsIgnoreCase("admin@ytu.com")) {
                            editor.putString("userType","admin");
                            editor.apply();
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                startActivity(intent);
                                finish();
                            },1000);
                        }
                        else{
                            editor.putString("userType","user");
                            editor.apply();
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            },1000);
                        }
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
            email.setError("E-postayı giriniz");
            validate = false;
        }
        else {
            email.setError(null);
        }

        if(userEnteredPassword.isEmpty()) {
            password.setError("Şifreyi giriniz");
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

        resetPassword.setOnClickListener(view -> {
            userEnteredEmail = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
            if(userEnteredEmail.isEmpty()) {
                email.setError("E-postayı giriniz");
            }
            else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(userEnteredEmail).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        email.getEditText().setText("");
                        Objects.requireNonNull(password.getEditText()).setText("");
                        Toast.makeText(LoginActivity.this, "Sıfırlama maili gönderildi", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Bir hata oluştu", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void defineVariables() {
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        resetPassword = findViewById(R.id.resetPassword);
    }
}