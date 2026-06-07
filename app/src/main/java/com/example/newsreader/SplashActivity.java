package com.example.newsreader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private SettingsManager settingsManager;

    private static final String[] ALL_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    results -> {
                        // Mark complete regardless of grant/deny — user can change in Settings
                        settingsManager.setFirstLaunchComplete();
                        checkInternetAndProceed();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsManager = new SettingsManager(this);
        settingsManager.applyLanguage(settingsManager.getAppLanguage());

        int mode = settingsManager.getThemeMode();
        if (mode == SettingsManager.THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == SettingsManager.THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (settingsManager.isWelcomeVoiceEnabled()) {
            tts = new TextToSpeech(getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale(settingsManager.getAppLanguage()));
                    if (!isFinishing()) {
                        tts.speak(getString(R.string.welcome_to_newsreader),
                                TextToSpeech.QUEUE_FLUSH, null, "welcome_id");
                    }
                }
            });
        }

        if (settingsManager.isFirstLaunch()) {
            // Show splash for 800 ms so user sees the logo, then request permissions
            new Handler(Looper.getMainLooper()).postDelayed(
                    this::requestFirstLaunchPermissions, 800);
        } else {
            // Returning user — full 2.5 s splash then proceed
            new Handler(Looper.getMainLooper()).postDelayed(
                    this::checkInternetAndProceed, 2500);
        }
    }

    private void requestFirstLaunchPermissions() {
        List<String> toRequest = new ArrayList<>();
        for (String perm : ALL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(perm);
            }
        }
        if (!toRequest.isEmpty()) {
            permissionLauncher.launch(toRequest.toArray(new String[0]));
        } else {
            // All already granted (e.g. re-install on same device)
            settingsManager.setFirstLaunchComplete();
            checkInternetAndProceed();
        }
    }

    private void checkInternetAndProceed() {
        if (isFinishing()) return;
        if (isNetworkAvailable()) {
            startActivity(new Intent(SplashActivity.this, HomepageActivity.class));
            finish();
        } else {
            Toast.makeText(this,
                    "No internet connection. The app will close.",
                    Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 3000);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities cap =
                    cm.getNetworkCapabilities(cm.getActiveNetwork());
            return cap != null &&
                    (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}