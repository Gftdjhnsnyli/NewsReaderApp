package com.example.newsreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class SettingsManager {
    private static final String PREFS_NAME = "news_reader_prefs";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_FONT_TYPE = "font_type";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    
    // New Keys
    private static final String KEY_APP_LANGUAGE = "app_language";
    private static final String KEY_WELCOME_VOICE_ENABLED = "welcome_voice_enabled";

    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_SYSTEM = 0;

    private final SharedPreferences prefs;
    private final Context context;

    public SettingsManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setFontSize(int size) {
        prefs.edit().putInt(KEY_FONT_SIZE, size).apply();
    }

    public int getFontSize() {
        return prefs.getInt(KEY_FONT_SIZE, 16);
    }

    public void setFontType(String type) {
        prefs.edit().putString(KEY_FONT_TYPE, type).apply();
    }

    public String getFontType() {
        return prefs.getString(KEY_FONT_TYPE, "Sans Serif");
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveUserProfile(String name, String email) {
        prefs.edit().putString(KEY_USER_NAME, name).putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "John Doe");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "john.doe@reader.com");
    }

    public void clearUserProfile() {
        prefs.edit().remove(KEY_USER_NAME).remove(KEY_USER_EMAIL).apply();
    }

    // New Settings Methods
    public void setAppLanguage(String langCode) {
        prefs.edit().putString(KEY_APP_LANGUAGE, langCode).apply();
        applyLanguage(langCode);
    }

    public String getAppLanguage() {
        return prefs.getString(KEY_APP_LANGUAGE, "en");
    }

    public void setWelcomeVoiceEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_WELCOME_VOICE_ENABLED, enabled).apply();
    }

    public boolean isWelcomeVoiceEnabled() {
        return prefs.getBoolean(KEY_WELCOME_VOICE_ENABLED, false);
    }

    public void applyLanguage(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        context.createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}