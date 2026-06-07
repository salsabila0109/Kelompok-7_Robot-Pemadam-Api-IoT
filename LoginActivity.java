package com.example.robotpemadamapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etKey;
    Button btnLogin;
    ImageView btnEye;

    boolean isPasswordVisible = false;

    private final String VALID_KEY = "12345";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // CEK SUDAH LOGIN ATAU BELUM
        SharedPreferences prefs = getSharedPreferences("LOGIN", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        etKey = findViewById(R.id.etKey);
        btnLogin = findViewById(R.id.btnLogin);
        btnEye = findViewById(R.id.btnEye);

        // TOGGLE PASSWORD
        btnEye.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etKey.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnEye.setImageResource(R.drawable.icon_eye_off);
                isPasswordVisible = false;
            } else {
                etKey.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnEye.setImageResource(R.drawable.icon_eye);
                isPasswordVisible = true;
            }

            // biar cursor tetap di akhir
            etKey.setSelection(etKey.getText().length());
        });

        // LOGIN
        btnLogin.setOnClickListener(view -> {

            String inputKey = etKey.getText().toString().trim();

            if (inputKey.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Key tidak boleh kosong!", Toast.LENGTH_SHORT).show();

            } else if (inputKey.equals(VALID_KEY)) {

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();

            } else {
                Toast.makeText(LoginActivity.this, "Key salah!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}