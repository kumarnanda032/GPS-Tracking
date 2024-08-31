package com.example.gpstracking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.os.Handler;

public class UserActivity extends AppCompatActivity {
    private static final int REFRESH_INTERVAL = 5000; // 5 seconds
    Button btnSendGps, user_logout;
    FusedLocationProviderClient fusedLocationProviderClient;
    public double currentLatitude = 0.0;
    public double currentLongitude = 0.0;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        btnSendGps = findViewById(R.id.btnSendGps);
        user_logout = findViewById(R.id.user_logout);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(UserActivity.this);

        final SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeColors(Color.BLACK);
        refreshLayout.setOnRefreshListener(() -> {
            getLocation();
            refreshLayout.setRefreshing(false);
        });

        btnSendGps.setOnClickListener(v -> getLocation());

        user_logout.setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, LoginActivity.class));
            preferences.clearData(UserActivity.this);
            finish();
        });

        // Start the periodic task
        startRepeatingTask();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(UserActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                Toast.makeText(UserActivity.this, "currentLatitude: " + currentLatitude + "\ncurrentLongitude: " + currentLongitude, Toast.LENGTH_SHORT).show();
                sendLocationToServer(currentLatitude, currentLongitude);
            } else {
                Toast.makeText(UserActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendLocationToServer(double latitude, double longitude) {
        SharedPreferences user_sp = getSharedPreferences("user_sp", Context.MODE_PRIVATE);
        String user = user_sp.getString("USER", "");
        Toast.makeText(this, "user name: " + user, Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.11.78/SwitchBoardApp/save_location.php"); // Replace with your server URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String data = "user_id=" + user
                        + "&latitude=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
                        + "&longitude=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                runOnUiThread(() -> {
                    Toast.makeText(UserActivity.this, "Response from server: " + response.toString(), Toast.LENGTH_SHORT).show();
                    Log.e("res", "Server: " + response.toString());
                });
            } catch (Exception e) {
                Log.e("GPS", "Error: " + e.getMessage(), e);
            }
        }).start();
    }

    private void startRepeatingTask() {
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    getLocation();  // Get location and send to server
                } finally {
                    handler.postDelayed(runnable, REFRESH_INTERVAL);  // Schedule next run
                }
            }
        };
        runnable.run();  // Start the first run
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            Toast.makeText(UserActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);  // Stop the task when the activity is destroyed
    }
}
