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
        RadioGroup rgTheme = findViewById(R.id.rg_theme_settings);
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

        // Load current theme setting
        int currentTheme = settingsManager.getThemeMode();
        if (currentTheme == SettingsManager.THEME_LIGHT) {
            rgTheme.check(R.id.rb_theme_light);
        } else if (currentTheme == SettingsManager.THEME_DARK) {
            rgTheme.check(R.id.rb_theme_dark);
        } else {
            rgTheme.check(R.id.rb_theme_system);
        }

        switchVoice.setChecked(settingsManager.isWelcomeVoiceEnabled());

        findViewById(R.id.btn_system_font).setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(intent);
        });

        findViewById(R.id.btn_save_settings).setOnClickListener(v -> {
            // Save language
            String selectedLang;
            int checkedLangId = rgLanguage.getCheckedRadioButtonId();
            if (checkedLangId == R.id.rb_spanish) {
                selectedLang = "es";
            } else if (checkedLangId == R.id.rb_french) {
                selectedLang = "fr";
            } else if (checkedLangId == R.id.rb_arabic) {
                selectedLang = "ar";
            } else {
                selectedLang = "en";
            }
            settingsManager.setAppLanguage(selectedLang);
            
            // Save theme
            int selectedTheme = SettingsManager.THEME_SYSTEM;
            int checkedThemeId = rgTheme.getCheckedRadioButtonId();
            if (checkedThemeId == R.id.rb_theme_light) {
                selectedTheme = SettingsManager.THEME_LIGHT;
            } else if (checkedThemeId == R.id.rb_theme_dark) {
                selectedTheme = SettingsManager.THEME_DARK;
            }
            settingsManager.setThemeMode(selectedTheme);
            
            // Save voice setting
            settingsManager.setWelcomeVoiceEnabled(switchVoice.isChecked());
            
            Toast.makeText(this, R.string.save_settings, Toast.LENGTH_SHORT).show();
            
            // Restart App to apply changes globally
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}