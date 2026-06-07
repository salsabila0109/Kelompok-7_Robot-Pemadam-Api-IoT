package com.example.robotpemadamapi;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.graphics.Color;

public class KontrolRobotActivity extends AppCompatActivity {

    Button btnUp, btnDown, btnLeft, btnRight, btnSemprot;

    Switch switchStop;

    TextView tvApi, tvJarak, tvBaterai;

    ImageView btnBack;

    LinearLayout layoutSourcePopup;

    Button btnSource, btnCancel, btnYes;

    EditText etSource;

    WebView webViewCamera;

    DatabaseReference ref;

    // MQTT
    MqttClient mqttClient;

    String serverUri = "tcp://broker.hivemq.com:1883";

    String clientId = MqttClient.generateClientId();

    String topicKontrol = "robotpemadam/control";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kontrol_robot);

        // FIX MQTT THREAD
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder()
                        .permitAll()
                        .build();

        StrictMode.setThreadPolicy(policy);

        // =========================
        // UI
        // =========================

        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnSemprot = findViewById(R.id.btnSemprot);

        switchStop = findViewById(R.id.switchStop);

        tvApi = findViewById(R.id.tvApi);
        tvJarak = findViewById(R.id.tvJarak);
        tvBaterai = findViewById(R.id.tvBaterai);

        btnBack = findViewById(R.id.btnBack);

        layoutSourcePopup =
                findViewById(R.id.layoutSourcePopup);

        btnSource = findViewById(R.id.btnSource);
        btnCancel = findViewById(R.id.btnCancel);
        btnYes = findViewById(R.id.btnYes);

        etSource = findViewById(R.id.etSource);

        // =========================
        // WEBVIEW CAMERA
        // =========================

        webViewCamera =
                findViewById(R.id.webViewCamera);

        WebSettings webSettings =
                webViewCamera.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webViewCamera.setWebViewClient(
                new WebViewClient()
        );

        webViewCamera.setWebChromeClient(
                new WebChromeClient()
        );

        // CAMERA
        webViewCamera.loadUrl(
                "http://10.110.8.147:4747/video"
        );

        // =========================
        // FIREBASE
        // =========================

        ref = FirebaseDatabase.getInstance()
                .getReference("robots/00111");

        // =========================
        // MQTT
        // =========================

        connectMQTT();

        // =========================
        // SENSOR
        // =========================

        ref.child("sensor")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot
                            ) {

                                Boolean api =
                                        snapshot.child("api")
                                                .getValue(Boolean.class);

                                Double jarak =
                                        snapshot.child("jarak")
                                                .getValue(Double.class);

                                Integer baterai =
                                        snapshot.child("baterai")
                                                .getValue(Integer.class);

                                tvApi.setText(
                                        (api != null && api)
                                                ? "Api: Terdeteksi 🔥"
                                                : "Api: Tidak Terdeteksi"
                                );

                                tvJarak.setText(
                                        (jarak != null)
                                                ? "Jarak: " + jarak + " cm"
                                                : "Jarak: -"
                                );

                                if (baterai != null) {

                                    tvBaterai.setText(
                                            "Baterai: "
                                                    + baterai
                                                    + "%"
                                    );

                                } else {

                                    tvBaterai.setText(
                                            "Baterai: -"
                                    );
                                }
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error
                            ) {

                            }
                        });

        // =========================
        // SWITCH MODE
        // =========================

        switchStop.setChecked(false);

        switchStop.setText("MANUAL");

        switchStop.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        switchStop.setText("AUTO");

                        ref.child("kontrol")
                                .child("mode")
                                .setValue("AUTO");

                        switchStop.setTrackTintList(
                                ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                                this,
                                                android.R.color.holo_green_light
                                        )
                                )
                        );

                    } else {

                        switchStop.setText("MANUAL");

                        ref.child("kontrol")
                                .child("mode")
                                .setValue("MANUAL");

                        switchStop.setTrackTintList(
                                ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                                this,
                                                android.R.color.darker_gray
                                        )
                                )
                        );
                    }
                });

        // =========================
        // BACK
        // =========================

        btnBack.setOnClickListener(v -> finish());

        // =========================
        // CONTROL ROBOT
        // =========================

        setupTouch(btnUp, "MAJU");

        setupTouch(btnDown, "MUNDUR");

        setupTouch(btnLeft, "KIRI");

        setupTouch(btnRight, "KANAN");

        // =========================
        // SEMPROT AIR
        // =========================

        setupTouch(btnSemprot, "AIR");

        // =========================
        // SOURCE BUTTON
        // =========================

        btnSource.setOnClickListener(v ->
                layoutSourcePopup.setVisibility(View.VISIBLE)
        );

        btnCancel.setOnClickListener(v ->
                layoutSourcePopup.setVisibility(View.GONE)
        );

        btnYes.setOnClickListener(v -> {

            String source =
                    etSource.getText()
                            .toString()
                            .trim();

            if (source.isEmpty()) {

                Toast.makeText(
                        this,
                        "Source kosong",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (!source.startsWith("http://")
                    && !source.startsWith("https://")) {

                source = "http://" + source;
            }

            if (!source.endsWith("/video")) {

                source += "/video";
            }

            webViewCamera.loadUrl(source);

            layoutSourcePopup.setVisibility(View.GONE);
        });
    }

    // =========================
    // MQTT CONNECT
    // =========================

    private void connectMQTT() {

        try {

            mqttClient = new MqttClient(
                    serverUri,
                    clientId,
                    null
            );

            MqttConnectOptions options =
                    new MqttConnectOptions();

            options.setAutomaticReconnect(true);

            options.setCleanSession(true);

            mqttClient.connect(options);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================
    // TOUCH CONTROL
    // =========================

    private void setupTouch(
            Button button,
            String arah
    ) {

        button.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    button.setBackgroundTintList(
                            ColorStateList.valueOf(
                                    Color.parseColor("#8EEBFF")));

                    kirim(arah);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    button.setBackgroundTintList(
                            ColorStateList.valueOf(
                                    Color.parseColor("#3AA6B9")));

                    kirim("STOP");
                    break;
            }

            return true;
        });
    }

    // =========================
    // MQTT SEND
    // =========================

    private void kirim(String arah) {

        try {

            if (mqttClient != null
                    && mqttClient.isConnected()) {

                MqttMessage message =
                        new MqttMessage(
                                arah.getBytes()
                        );

                mqttClient.publish(
                        topicKontrol,
                        message
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================
    // DESTROY
    // =========================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        try {

            if (mqttClient != null
                    && mqttClient.isConnected()) {

                mqttClient.disconnect();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}