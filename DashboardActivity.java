package com.example.robotpemadamapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.anastr.speedviewlib.SpeedView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    // =========================
    // 🔥 STATUS SENSOR
    // =========================
    TextView tvStatus, tvApi, tvJarak;

    // =========================
    // 🔥 GAUGE
    // =========================
    SpeedView gaugeApi;
    SpeedView gaugeJarak;

    // =========================
    // 🔥 BUTTON & MENU
    // =========================
    LinearLayout btnKontrol;
    LinearLayout layoutMenu;

    LinearLayout menuHistory;
    LinearLayout menuLogout;
    LinearLayout menuTentang;

    ImageView btnMenu;

    // =========================
    // 🔥 FIREBASE
    // =========================
    DatabaseReference ref;

    // =========================
    // 🔥 FILTER HISTORY
    // =========================
    Boolean lastApi = null;
    Double lastJarak = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // =========================
        // 🔥 FIREBASE
        // =========================
        ref = FirebaseDatabase.getInstance()
                .getReference("robots/00111");

        // =========================
        // 🔥 INIT VIEW
        // =========================
        tvStatus = findViewById(R.id.tvStatus);
        tvApi = findViewById(R.id.tvApi);
        tvJarak = findViewById(R.id.tvJarak);

        // =========================
        // 🔥 GAUGE
        // =========================
        gaugeApi = findViewById(R.id.gaugeApi);
        gaugeJarak = findViewById(R.id.gaugeJarak);

        // =========================
        // 🔥 BUTTON
        // =========================
        btnKontrol = findViewById(R.id.btnKontrol);
        btnMenu = findViewById(R.id.btnMenu);

        // =========================
        // 🔥 MENU
        // =========================
        layoutMenu = findViewById(R.id.layoutMenu);

        menuHistory = findViewById(R.id.menuHistory);
        menuLogout = findViewById(R.id.menuLogout);
        menuTentang = findViewById(R.id.menuTentang);

        // =========================
        // 🔥 SETUP GAUGE API
        // =========================
        gaugeApi.setMinSpeed(0);
        gaugeApi.setMaxSpeed(100);
        gaugeApi.setUnit("");
        gaugeApi.setWithTremble(false);
        gaugeApi.speedTo(0);

        // =========================
        // 🔥 SETUP GAUGE JARAK
        // =========================
        gaugeJarak.setMinSpeed(0);
        gaugeJarak.setMaxSpeed(100);
        gaugeJarak.setUnit("cm");
        gaugeJarak.setWithTremble(false);
        gaugeJarak.speedTo(0);

        // =========================
        // 🔥 REALTIME SENSOR
        // =========================
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    // =========================
                    // 🔥 SENSOR
                    // =========================
                    Boolean api = snapshot.child("sensor")
                            .child("api")
                            .getValue(Boolean.class);

                    Double jarak = snapshot.child("sensor")
                            .child("jarak")
                            .getValue(Double.class);

                    Long lastUpdate = snapshot
                            .child("lastUpdate")
                            .getValue(Long.class);

                    // =========================
                    // 🔥 STATUS ROBOT
                    // =========================
                    long now = System.currentTimeMillis();

                    if (lastUpdate != null &&
                            (now - lastUpdate <= 10000)) {

                        tvStatus.setText("Aktif");

                    } else {

                        tvStatus.setText("Tidak Aktif");
                    }

                    // =========================
                    // 🔥 STATUS API
                    // =========================
                    if (api != null && api) {

                        tvApi.setText("Api\nTerdeteksi");

                    } else {

                        tvApi.setText("Tidak\nTerdeteksi");
                    }

                    // =========================
                    // 🔥 GAUGE API
                    // =========================
                    if (api != null && api) {

                        gaugeApi.speedTo(100);

                    } else {

                        gaugeApi.speedTo(0);
                    }

                    // =========================
                    // 🔥 JARAK
                    // =========================
                    if (jarak != null) {

                        tvJarak.setText(jarak + " cm");

                    } else {

                        tvJarak.setText("-");
                    }

                    // =========================
                    // 🔥 GAUGE JARAK
                    // =========================
                    if (jarak != null) {

                        if (jarak > 100) {
                            jarak = 100.0;
                        }

                        gaugeJarak.speedTo(jarak.floatValue());

                    } else {

                        gaugeJarak.speedTo(0);
                    }

                    // =========================
                    // 🔥 SIMPAN KE HISTORY
                    // =========================
                    if (api != null && jarak != null) {

                        boolean apiChanged =
                                lastApi == null ||
                                        !lastApi.equals(api);

                        boolean jarakChanged =
                                lastJarak == null ||
                                        Math.abs(lastJarak - jarak) >= 5;

                        if (apiChanged || jarakChanged) {

                            long timestamp =
                                    System.currentTimeMillis();

                            DatabaseReference logRef =
                                    ref.child("log").push();

                            logRef.child("api")
                                    .setValue(api);

                            logRef.child("jarak")
                                    .setValue(jarak);

                            logRef.child("timestamp")
                                    .setValue(timestamp);

                            lastApi = api;
                            lastJarak = jarak;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Toast.makeText(
                        DashboardActivity.this,
                        "Gagal mengambil data",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =========================
        // 🔥 BUTTON KONTROL
        // =========================
        btnKontrol.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            DashboardActivity.this,
                            KontrolRobotActivity.class
                    )
            );
        });

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
        // 🔥 MENU HISTORY
        // =========================
        menuHistory.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    HistoryActivity.class
            );

            startActivity(intent);

            layoutMenu.setVisibility(View.GONE);
        });

        // =========================
        // 🔥 MENU LOGOUT
        // =========================
        menuLogout.setOnClickListener(v -> {

            layoutMenu.setVisibility(View.GONE);

            showLogoutDialog();
        });

        // =========================
        // 🔥 MENU TENTANG
        // =========================
        menuTentang.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    TentangActivity.class
            );

            startActivity(intent);

            layoutMenu.setVisibility(View.GONE);
        });
    }

    // =========================
    // 🔥 DIALOG LOGOUT
    // =========================
    private void showLogoutDialog() {

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
                                    DashboardActivity.this,
                                    LoginActivity.class
                            );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);

                    Toast.makeText(
                            DashboardActivity.this,
                            "Berhasil logout",
                            Toast.LENGTH_SHORT
                    ).show();
                })

                .setNegativeButton("Batal", null)
                .show();
    }
}