package com.example.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GoogleAuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_auth);

        ProgressBar progressBar = findViewById(R.id.authProgress);

        findViewById(R.id.accountItem).setOnClickListener(v -> {
            // Simulate professional linking handshake
            progressBar.setVisibility(View.VISIBLE);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Toast.makeText(this, "Linked successfully with Google!", Toast.LENGTH_SHORT).show();
                
                SettingsManager settingsManager = new SettingsManager(this);
                settingsManager.setLoggedIn(true);
                
                Intent intent = new Intent(GoogleAuthActivity.this, HomepageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("IS_GOOGLE_LINKED", true);
                intent.putExtra("GOOGLE_EMAIL", "john.doe@gmail.com");
                
                startActivity(intent);
                finish();
            }, 1500);
        });
    }
}