package com.example.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settingsManager = new SettingsManager(this);
        settingsManager.applyLanguage(settingsManager.getAppLanguage());
        
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        RadioGroup rgLanguage = findViewById(R.id.rg_language);
        SwitchMaterial switchVoice = findViewById(R.id.switch_welcome_voice);

        // Load current language setting
        String currentLang = settingsManager.getAppLanguage();
        if ("es".equals(currentLang)) {
            rgLanguage.check(R.id.rb_spanish);
        } else if ("fr".equals(currentLang)) {
            rgLanguage.check(R.id.rb_french);
        } else if ("ar".equals(currentLang)) {
            rgLanguage.check(R.id.rb_arabic);
        } else {
            rgLanguage.check(R.id.rb_english);
        }

        switchVoice.setChecked(settingsManager.isWelcomeVoiceEnabled());

        findViewById(R.id.btn_save_settings).setOnClickListener(v -> {
            // Determine language selection
            String selectedLang;
            int checkedId = rgLanguage.getCheckedRadioButtonId();
            if (checkedId == R.id.rb_spanish) {
                selectedLang = "es";
            } else if (checkedId == R.id.rb_french) {
                selectedLang = "fr";
            } else if (checkedId == R.id.rb_arabic) {
                selectedLang = "ar";
            } else {
                selectedLang = "en";
            }
            
            settingsManager.setAppLanguage(selectedLang);
            
            // Save voice setting
            settingsManager.setWelcomeVoiceEnabled(switchVoice.isChecked());
            
            Toast.makeText(this, R.string.save_settings, Toast.LENGTH_SHORT).show();
            
            // Restart App to apply changes globally and clear any layout state
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}