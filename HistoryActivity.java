package com.example.robotpemadamapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    LinearLayout historyContainer;
    LinearLayout layoutMenu;

    ImageView btnMenu, btnDelete;

    LinearLayout menuDashboard;
    LinearLayout menuTentang;
    LinearLayout menuLogout;

    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // =========================
        // 🔥 INISIALISASI
        // =========================

        historyContainer = findViewById(R.id.historyContainer);

        layoutMenu = findViewById(R.id.layoutMenu);

        btnMenu = findViewById(R.id.btnMenu);
        btnDelete = findViewById(R.id.btnDelete);

        menuDashboard = findViewById(R.id.menuDashboard);
        menuTentang = findViewById(R.id.menuTentang);
        menuLogout = findViewById(R.id.menuLogout);

        ref = FirebaseDatabase.getInstance()
                .getReference("robots/00111/log");

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
        // 🔥 MENU DASHBOARD
        // =========================

        menuDashboard.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            HistoryActivity.this,
                            DashboardActivity.class
                    )
            );
        });

        // =========================
        // 🔥 MENU TENTANG
        // =========================

        menuTentang.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            HistoryActivity.this,
                            TentangActivity.class
                    )
            );
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
                                        HistoryActivity.this,
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

        // =========================
        // 🔥 HAPUS SEMUA HISTORY
        // =========================

        btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Hapus History")
                    .setMessage("Yakin ingin menghapus semua history?")
                    .setPositiveButton("Ya", (dialog, which) -> {

                        ref.removeValue()
                                .addOnSuccessListener(unused -> {

                                    historyContainer.removeAllViews();

                                    Toast.makeText(
                                            HistoryActivity.this,
                                            "History berhasil dihapus",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                })

                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            HistoryActivity.this,
                                            "Gagal menghapus history",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                });
                    })

                    .setNegativeButton("Batal", null)
                    .show();
        });

        // =========================
        // 🔥 AUTO HAPUS > 3 HARI
        // =========================

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_YEAR, -3);

        long threeDaysAgo = calendar.getTimeInMillis();

        // HAPUS DATA LAMA
        ref.orderByChild("timestamp")
                .endAt(threeDaysAgo)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        for (DataSnapshot data : snapshot.getChildren()) {

                            data.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(
                                HistoryActivity.this,
                                "Gagal menghapus data lama",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

        // =========================
        // 🔥 AMBIL DATA 3 HARI TERAKHIR
        // =========================

        Query query = ref.orderByChild("timestamp")
                .startAt(threeDaysAgo);

        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                historyContainer.removeAllViews();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Boolean api = data.child("api")
                            .getValue(Boolean.class);

                    Double jarak = data.child("jarak")
                            .getValue(Double.class);

                    Long timestamp = data.child("timestamp")
                            .getValue(Long.class);

                    View itemView = LayoutInflater.from(
                                    HistoryActivity.this)
                            .inflate(
                                    R.layout.item_history,
                                    historyContainer,
                                    false
                            );

                    TextView tvWaktu =
                            itemView.findViewById(R.id.tvWaktu);

                    TextView tvApi =
                            itemView.findViewById(R.id.tvApi);

                    TextView tvJarak =
                            itemView.findViewById(R.id.tvJarak);

                    // =========================
                    // 🔥 FORMAT WAKTU
                    // =========================

                    if (timestamp != null) {

                        Date date = new Date(timestamp);

                        SimpleDateFormat sdf =
                                new SimpleDateFormat(
                                        "HH:mm dd MMM yyyy",
                                        Locale.getDefault()
                                );

                        tvWaktu.setText(sdf.format(date));
                    }

                    // =========================
                    // 🔥 STATUS API
                    // =========================

                    if (api != null && api) {

                        tvApi.setText("Terdeteksi");

                    } else {

                        tvApi.setText("Tidak Terdeteksi");
                    }

                    // =========================
                    // 🔥 STATUS JARAK
                    // =========================

                    if (jarak != null) {

                        tvJarak.setText(jarak + " m");

                    } else {

                        tvJarak.setText("-");
                    }

                    // DATA TERBARU MUNCUL PALING ATAS
                    historyContainer.addView(itemView, 0);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Toast.makeText(
                        HistoryActivity.this,
                        "Gagal mengambil history",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}