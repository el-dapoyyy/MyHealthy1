package com.example.myhealthy;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends Activity {

    // Waktu tunggu setelah lokasi ditampilkan sebelum pindah ke Login
    private static final int DISPLAY_PAUSE_MS   = 4000;
    // Batas maksimal menunggu GPS (jika tidak berhasil, langsung lanjut)
    private static final int MAX_WAIT_MS         = 10000;

    private static final int REQUEST_LOCATION_PERM = 100;
    private static final int REQUEST_NOTIF_PERM    = 101;
    private static final String CHANNEL_ID          = "location_channel";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean navigated = false;

    private TextView greetingText;

    // Fallback: jika lokasi tidak berhasil dalam MAX_WAIT_MS, langsung ke Login
    private final Runnable timeoutRunnable = this::navigateToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        greetingText = findViewById(R.id.greeting_text);


        // Tampilkan teks sementara selagi menunggu GPS
        greetingText.setText("Mendeteksi lokasi...");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();

        // Mulai timeout maksimal
        handler.postDelayed(timeoutRunnable, MAX_WAIT_MS);

        // Mulai proses lokasi
        requestLocationPermission();
    }

    // ─────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────

    private void navigateToLogin() {
        if (!navigated) {
            navigated = true;
            handler.removeCallbacksAndMessages(null);
            stopLocationUpdates();

            // Cek apakah user sudah login via Firebase
            com.google.firebase.auth.FirebaseAuth auth =
                    com.google.firebase.auth.FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                // Sudah login → langsung ke Menu
                startActivity(new Intent(SplashActivity.this, MenuActivity.class));
            } else {
                // Belum login → ke halaman Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }
    }

    /** Dipanggil setelah lokasi ditampilkan — tunggu sebentar lalu pindah */
    private void navigateAfterDisplay() {
        handler.removeCallbacks(timeoutRunnable); // batalkan timeout
        handler.postDelayed(this::navigateToLogin, DISPLAY_PAUSE_MS);
    }

    // ─────────────────────────────────────────────────────────────
    // Permission handling
    // ─────────────────────────────────────────────────────────────

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERM
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
            // Jika ditolak, timeout akan tetap berjalan dan langsung ke Login
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Location retrieval
    // ─────────────────────────────────────────────────────────────

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                reverseGeocode(location);
            } else {
                requestFreshLocation();
            }
        });
    }

    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) reverseGeocode(location);
                stopLocationUpdates();
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Reverse Geocoding (background thread)
    // ─────────────────────────────────────────────────────────────

    private void reverseGeocode(Location location) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(SplashActivity.this, new Locale("id", "ID"));
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);

                    String desa      = nvl(addr.getSubLocality());
                    String kecamatan = nvl(addr.getSubAdminArea());
                    String kabupaten = nvl(addr.getAdminArea());
                    String provinsi  = nvl(addr.getLocality());
                    String negara    = nvl(addr.getCountryName());
                    String kodePos   = nvl(addr.getPostalCode());

                    // Baris 1: Desa, Kecamatan, Kabupaten, Provinsi
                    String line1 = join(", ", desa, kecamatan, kabupaten, provinsi);
                    // Baris 2: Negara Kodepos
                    String line2 = negara + (kodePos.isEmpty() ? "" : " " + kodePos);
                    final String displayText = (line1.isEmpty() ? "" : line1 + "\n") + line2;

                    // Format satu baris untuk notifikasi
                    String notifText = join(", ", desa, kecamatan, kabupaten, provinsi, negara)
                            + (kodePos.isEmpty() ? "" : " " + kodePos);

                    handler.post(() -> {
                        greetingText.setText(displayText);
                        showLocationNotification(notifText);
                        navigateAfterDisplay();
                    });
                } else {
                    handler.post(() -> {
                        greetingText.setText("Lokasi tidak ditemukan");
                        navigateAfterDisplay();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    greetingText.setText("Gagal mendapatkan lokasi");
                    navigateAfterDisplay();
                });
            }
        }).start();
    }

    private static String nvl(String s) {
        return s != null ? s.trim() : "";
    }

    private static String join(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(sep);
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────
    // Notification
    // ─────────────────────────────────────────────────────────────

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Informasi Lokasi", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifikasi lokasi pengguna saat ini");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void showLocationNotification(String address) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIF_PERM);
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle("📍 Lokasi Anda Saat Ini")
                .setContentText(address)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(address))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(1001, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        stopLocationUpdates();
    }
}
