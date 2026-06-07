package com.example.robotpemadamapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class TentangActivity extends AppCompatActivity {

    ImageView btnMenu;

    LinearLayout layoutMenu;

    LinearLayout menuHistory;
    LinearLayout menuLogout;
    LinearLayout menuDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tentang);

        // =========================
        // 🔥 INIT
        // =========================
        btnMenu = findViewById(R.id.btnMenu);

        layoutMenu = findViewById(R.id.layoutMenu);

        menuHistory = findViewById(R.id.menuHistory);
        menuLogout = findViewById(R.id.menuLogout);
        menuDashboard = findViewById(R.id.menuDashboard);

        // =========================
        // 🔥 BURGER MENU
        // =========================
        btnMenu.setOnClickListener(v -> {

            if (layoutMenu.getVisibility() == View.GONE) {

                layoutMenu.setVisibility(View.VISIBLE);

            } else {

                layoutMenu.setVisibility(View.GONE);
            }
        });

        // =========================
        // 🔥 HISTORY
        // =========================
        menuHistory.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            TentangActivity.this,
                            HistoryActivity.class
                    )
            );

            layoutMenu.setVisibility(View.GONE);
        });

        // =========================
        // 🔥 DASHBOARD
        // =========================
        menuDashboard.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TentangActivity.this,
                    DashboardActivity.class
            );

            startActivity(intent);

            layoutMenu.setVisibility(View.GONE);
        });

        // =========================
        // 🔥 LOGOUT
        // =========================
        menuLogout.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Yakin ingin keluar?")
                    .setPositiveButton("Ya", (dialog, which) -> {

                        SharedPreferences prefs =
                                getSharedPreferences(
                                        "LOGIN",
                                        MODE_PRIVATE
                                );

                        prefs.edit()
                                .putBoolean("isLoggedIn", false)
                                .apply();

                        Intent intent =
                                new Intent(
                                        TentangActivity.this,
                                        LoginActivity.class
                                );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                    })

                    .setNegativeButton("Batal", null)
                    .show();
        });
    }
}