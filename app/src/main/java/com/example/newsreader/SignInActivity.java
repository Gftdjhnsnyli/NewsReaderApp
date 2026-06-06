package com.example.newsreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    // ✅ REPLACE THIS with your Web Application Client ID from Google Cloud Console
    private static final String WEB_CLIENT_ID = "197897894392-s0bm5fn7mmvk07a19q86frthnh179mda.apps.googleusercontent.com";

    private CredentialManager credentialManager;
    private Executor mainExecutor;
    private SettingsManager settingsManager;

    // UI Elements for Email/Password login
    private EditText etEmail, etPassword;

    // Password Regex Rules:
    // (?=.*[0-9]) -> at least one number
    // (?=.*[^a-zA-Z0-9 ]) -> at least one special character (non-alphanumeric, non-space)
    // .{10,} -> at least 10 characters long
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[^a-zA-Z0-9 ]).{10,}$");

    // ✅ Permission Launcher
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
                    Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Some permissions denied. App features may be limited.", Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SettingsManager first so we can check first launch & internet
        settingsManager = new SettingsManager(this);

        // ✅ 1. INTERNET CHECK
        if (!NetworkUtils.isInternetAvailable(this)) {
            // Show the No Internet layout and STOP execution of the rest of onCreate
            setContentView(R.layout.layout_no_internet);
            findViewById(R.id.btn_retry).setOnClickListener(v -> recreate());
            return;
        }

        // ✅ 2. If internet is available, proceed to normal Sign In screen
        setContentView(R.layout.activity_sign_in);

        credentialManager = CredentialManager.create(this);
        mainExecutor = ContextCompat.getMainExecutor(this);

        // Initialize EditText fields
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        Button btnSignIn = findViewById(R.id.btnLogin);
        Button btnGuest = findViewById(R.id.btnGuest);
        TextView tvSignUp = findViewById(R.id.tvRegisterLink);

        if (btnGuest != null) {
            btnGuest.setOnClickListener(v -> {
                settingsManager.setLoggedIn(true);
                settingsManager.setGuestMode(true);
                settingsManager.saveUserProfile("Guest Profile", " ");

                Toast.makeText(this, "Continuing as Guest", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignInActivity.this, HomepageActivity.class));
                finish();
            });
        }

        if (btnSignIn != null) {
            // Credential validation
            btnSignIn.setOnClickListener(v -> handleEmailSignIn());
        }

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v ->
                    startActivity(new Intent(SignInActivity.this, SignUpActivity.class))
            );
        }

        // ✅ Google Sign-In
        findViewById(R.id.btn_google).setOnClickListener(v -> handleGoogleSignIn());

        findViewById(R.id.btn_facebook).setOnClickListener(v -> {
            Toast.makeText(this, "Please integrate Facebook SDK for real authentication.", Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btn_x).setOnClickListener(v -> {
            Toast.makeText(this, "Please integrate X (Twitter) SDK for real authentication.", Toast.LENGTH_LONG).show();
        });

        // ✅ 3. FIRST LAUNCH PERMISSIONS CHECK
        if (settingsManager.isFirstLaunch()) {
            requestInitialPermissions();
            settingsManager.setFirstLaunchComplete(); // Mark as complete so it doesn't ask again
        }
    }

    private void handleEmailSignIn() {
        if (etEmail == null || etPassword == null) {
            Log.e(TAG, "Email or Password EditText is null. Check your layout IDs.");
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Check if fields are empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validate password rules (Min 10 chars, 1 number, 1 special char)
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 10 characters, include a number and a special character.", Toast.LENGTH_LONG).show();
            return;
        }

        // 4. Log them in and clear guest mode
        settingsManager.setLoggedIn(true);
        settingsManager.setGuestMode(false); // ✅ Clear guest mode on real sign-in

        // Extract username from email (part before '@') or use the full email
        String username = email.contains("@") ? email.split("@")[0] : email;
        settingsManager.saveUserProfile(username, email);

        Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SignInActivity.this, HomepageActivity.class));
        finish();
    }

    private boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private void handleGoogleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Show all Google accounts on device
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false) // Always show account picker
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                mainExecutor,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignInSuccess(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Sign-in error: " + e.getMessage());
                        Toast.makeText(
                                SignInActivity.this,
                                "Sign-in failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void handleSignInSuccess(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;

            if (customCredential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(customCredential.getData());

                    String email = googleIdTokenCredential.getId();
                    String displayName = googleIdTokenCredential.getDisplayName();
                    // Fallback to the email prefix if display name is null to avoid generic names
                    String name = displayName != null ? displayName : email.split("@")[0];

                    settingsManager.setLoggedIn(true);
                    settingsManager.setGuestMode(false); // ✅ Clear guest mode on real sign-in
                    settingsManager.saveUserProfile(name, email);

                    Log.d(TAG, "Google Sign-In Success. Email: " + email);
                    Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(SignInActivity.this, HomepageActivity.class));
                    finish();

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing Google credential", e);
                    Toast.makeText(this, "Error processing Google account.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "Unexpected credential type: " + customCredential.getType());
                Toast.makeText(this, "Unexpected credential type.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "Credential is not a CustomCredential");
            Toast.makeText(this, "Unrecognized credential.", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Method to request all safe permissions on first launch
    private void requestInitialPermissions() {
        List<String> permissions = new ArrayList<>();

        // Handle OS version differences for Storage/Notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // Standard safe permissions
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        // Filter out permissions that are already granted to avoid errors
        List<String> permissionsToRequest = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(perm);
            }
        }

        // Launch the permission dialog if there are any to request
        if (!permissionsToRequest.isEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }
}