package com.example.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.newsreader.fragments.BookmarksFragment;
import com.example.newsreader.fragments.ExploreFragment;
import com.example.newsreader.fragments.FeedbackFragment;
import com.example.newsreader.fragments.FeedsFragment;
import com.example.newsreader.fragments.OfflineNewsFragment;
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

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.app_name);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed());
        }

        View notificationBtn = findViewById(R.id.btn_notification);
        if (notificationBtn != null) {
            notificationBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, NotificationsActivity.class)));
        }

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

        getSupportFragmentManager().addOnBackStackChangedListener(
                this::updateUIForCurrentFragment);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FeedsFragment())
                    .commit();
            if (rootView != null) {
                rootView.post(this::updateUIForCurrentFragment);
            }
        }

        // Permissions are handled in SplashActivity on first launch — nothing to do here.
    }

    private void updateUIForCurrentFragment() {
        Fragment current =
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        View appBar = findViewById(R.id.app_bar);
        TextView titleView = findViewById(R.id.toolbar_title);
        View notificationBtn = findViewById(R.id.btn_notification);

        if (getSupportActionBar() != null) {
            boolean isMain = current instanceof FeedsFragment ||
                    current instanceof ExploreFragment ||
                    current instanceof BookmarksFragment ||
                    current instanceof ProfileFragment;
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isMain);
        }

        if (current instanceof FeedsFragment) {
            bottomNav.getMenu().findItem(R.id.nav_feeds).setChecked(true);
            bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
            if (titleView != null) titleView.setText(R.string.app_name);
            if (notificationBtn != null) notificationBtn.setVisibility(View.VISIBLE);

        } else if (current instanceof ExploreFragment) {
            bottomNav.getMenu().findItem(R.id.nav_explore).setChecked(true);
            bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
            if (titleView != null) titleView.setText(R.string.title_explore);
            if (notificationBtn != null) notificationBtn.setVisibility(View.GONE);

        } else if (current instanceof BookmarksFragment) {
            bottomNav.getMenu().findItem(R.id.nav_bookmarks).setChecked(true);
            bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
            if (titleView != null) titleView.setText(R.string.title_bookmarks);
            if (notificationBtn != null) notificationBtn.setVisibility(View.GONE);

        } else if (current instanceof ProfileFragment) {
            bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
            bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
            if (titleView != null) titleView.setText(R.string.title_profile);
            if (notificationBtn != null) notificationBtn.setVisibility(View.GONE);

        } else if (current instanceof FeedbackFragment ||
                current instanceof OfflineNewsFragment) {
            // These sub-pages manage their own header; hide the shared app bar & nav
            bottomNav.setVisibility(View.GONE);
            if (appBar != null) appBar.setVisibility(View.GONE);
        }
        // AboutActivity is a separate Activity — it does not appear in the fragment
        // container, so no case needed here.
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

        if (itemId == R.id.menu_feedback) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FeedbackFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (itemId == R.id.menu_offline_news) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OfflineNewsFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (itemId == R.id.menu_about_us) {
            // AboutUs is now a full Activity
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}