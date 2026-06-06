package com.example.newsreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.newsreader.fragments.AboutUsFragment;
import com.example.newsreader.fragments.BookmarksFragment;
import com.example.newsreader.fragments.ExploreFragment;
import com.example.newsreader.fragments.FeedbackFragment;
import com.example.newsreader.fragments.FeedsFragment;
import com.example.newsreader.fragments.NotificationsFragment;
import com.example.newsreader.fragments.OfflineNewsFragment;
import com.example.newsreader.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomepageActivity extends AppCompatActivity {

    private SettingsManager settingsManager;
    private BottomNavigationView bottomNav;

    // ✅ Permission Launcher for initial setup
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            results -> {
                boolean allGranted = true;
                for (Map.Entry<String, Boolean> entry : results.entrySet()) {
                    if (!entry.getValue()) {
                        allGranted = false;
                        Log.w("Permissions", "Denied: " + entry.getKey());
                    }
                }
                if (allGranted) {
                    Toast.makeText(this, "Permissions granted. Enjoy the app!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settingsManager = new SettingsManager(this);
        applyTheme();
        
        // Consistent EdgeToEdge with Splash screen to prevent layout jumps
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_homepage);

        // Apply insets to the root view to avoid overlap with status/navigation bars
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

            toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        View notificationBtn = findViewById(R.id.btn_notification);
        if (notificationBtn != null) {
            notificationBtn.setOnClickListener(v -> getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NotificationsFragment())
                    .addToBackStack(null)
                    .commit());
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

        // Sync bottom navigation and toolbar visibility when backstack changes
        getSupportFragmentManager().addOnBackStackChangedListener(this::updateUIForCurrentFragment);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FeedsFragment())
                    .commit();
            // We need to call it manually for the initial fragment
            if (rootView != null) {
                rootView.post(this::updateUIForCurrentFragment);
            }
        }

        // ✅ Request permissions on first launch as the app starts
        if (settingsManager.isFirstLaunch()) {
            requestInitialPermissions();
            settingsManager.setFirstLaunchComplete();
        }
    }

    // ✅ Method to request required permissions
    private void requestInitialPermissions() {
        List<String> permissions = new ArrayList<>();

        // Since minSdk is 34, we only need the newer media permissions and basic ones
        permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> toRequest = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(perm);
            }
        }

        if (!toRequest.isEmpty()) {
            permissionLauncher.launch(toRequest.toArray(new String[0]));
        }
    }

    private void updateUIForCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        View appBar = findViewById(R.id.app_bar);
        TextView titleView = findViewById(R.id.toolbar_title);
        View notificationBtn = findViewById(R.id.btn_notification);
        
        if (currentFragment instanceof FeedsFragment) {
            bottomNav.getMenu().findItem(R.id.nav_feeds).setChecked(true);
            bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
            if (titleView != null) titleView.setText(R.string.app_name);
            if (notificationBtn != null) notificationBtn.setVisibility(View.VISIBLE);
        } else if (currentFragment instanceof ExploreFragment) {
            bottomNav.getMenu().findItem(R.id.nav_explore).setChecked(true);
            bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
            if (titleView != null) titleView.setText(R.string.title_explore);
            if (notificationBtn != null) notificationBtn.setVisibility(View.GONE);
        } else if (currentFragment instanceof BookmarksFragment) {
            bottomNav.getMenu().findItem(R.id.nav_bookmarks).setChecked(true);
            bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
            if (titleView != null) titleView.setText(R.string.title_bookmarks);
            if (notificationBtn != null) notificationBtn.setVisibility(View.GONE);
        } else if (currentFragment instanceof ProfileFragment) {
            bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
            bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
            if (titleView != null) titleView.setText(R.string.title_profile);
            if (notificationBtn != null) notificationBtn.setVisibility(View.GONE);
        } else if (currentFragment instanceof FeedbackFragment || currentFragment instanceof OfflineNewsFragment || currentFragment instanceof AboutUsFragment || currentFragment instanceof NotificationsFragment) {
            // These sub-pages handle their own headers for a more immersive feel
            bottomNav.setVisibility(View.GONE);
            if (appBar != null) appBar.setVisibility(View.GONE);
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
        Fragment selectedFragment = null;

        if (itemId == R.id.menu_feedback) {
            selectedFragment = new FeedbackFragment();
        } else if (itemId == R.id.menu_offline_news) {
            selectedFragment = new OfflineNewsFragment();
        } else if (itemId == R.id.menu_about_us) {
            selectedFragment = new AboutUsFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}