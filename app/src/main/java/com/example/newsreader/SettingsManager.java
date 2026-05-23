package com.example.newsreader;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "news_reader_prefs";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_FONT_TYPE = "font_type";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_SYSTEM = 0;

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
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
}