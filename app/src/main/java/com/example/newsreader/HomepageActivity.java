package com.example.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.newsreader.fragments.BookmarksFragment;
import com.example.newsreader.fragments.ExploreFragment;
import com.example.newsreader.fragments.FeedsFragment;
import com.example.newsreader.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomepageActivity extends AppCompatActivity {

    private SettingsManager settingsManager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settingsManager = new SettingsManager(this);
        applyTheme();
        
        setContentView(R.layout.activity_homepage);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_feeds) {
                selectedFragment = new FeedsFragment();
            } else if (itemId == R.id.nav_explore) {
                selectedFragment = new ExploreFragment();
            } else if (itemId == R.id.nav_bookmarks) {
                selectedFragment = new BookmarksFragment();
            } else if (itemId == R.id.nav_profile) {
                if (settingsManager.isLoggedIn()) {
                    selectedFragment = new ProfileFragment();
                } else {
                    startActivity(new Intent(this, SignInActivity.class));
                    return false;
                }
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
            }
            return true;
        });

        // Sync bottom navigation selection when backstack changes
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof FeedsFragment) {
                bottomNav.getMenu().findItem(R.id.nav_feeds).setChecked(true);
            } else if (currentFragment instanceof ExploreFragment) {
                bottomNav.getMenu().findItem(R.id.nav_explore).setChecked(true);
            } else if (currentFragment instanceof BookmarksFragment) {
                bottomNav.getMenu().findItem(R.id.nav_bookmarks).setChecked(true);
            } else if (currentFragment instanceof ProfileFragment) {
                bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FeedsFragment())
                    .commit();
        }
    }

    private void applyTheme() {
        int mode = settingsManager.getThemeMode();
        if (mode == SettingsManager.THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == SettingsManager.THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.theme_light) {
            settingsManager.setThemeMode(SettingsManager.THEME_LIGHT);
            applyTheme();
            return true;
        } else if (itemId == R.id.theme_dark) {
            settingsManager.setThemeMode(SettingsManager.THEME_DARK);
            applyTheme();
            return true;
        } else if (itemId == R.id.theme_system) {
            settingsManager.setThemeMode(SettingsManager.THEME_SYSTEM);
            applyTheme();
            return true;
        } else if (itemId == R.id.menu_font_style) {
            Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}