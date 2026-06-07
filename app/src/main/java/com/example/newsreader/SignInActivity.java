package com.example.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.concurrent.Executor;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    // Replace with your actual Web Client ID from Google Cloud Console
    private static final String WEB_CLIENT_ID =
            "197897894392-s0bm5fn7mmvk07a19q86frthnh179mda.apps.googleusercontent.com";

    private CredentialManager credentialManager;
    private Executor mainExecutor;
    private SettingsManager settingsManager;

    private EditText etEmail, etPassword;

    // Password must be ≥10 chars, contain a number, and a special character
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[^a-zA-Z0-9 ]).{10,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsManager = new SettingsManager(this);

        // Internet check — show retry screen if offline
        if (!NetworkUtils.isInternetAvailable(this)) {
            setContentView(R.layout.layout_no_internet);
            findViewById(R.id.btn_retry).setOnClickListener(v -> recreate());
            return;
        }

        setContentView(R.layout.activity_sign_in);

        credentialManager = CredentialManager.create(this);
        mainExecutor      = ContextCompat.getMainExecutor(this);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        Button   btnSignIn = findViewById(R.id.btnLogin);
        Button   btnGuest  = findViewById(R.id.btnGuest);
        TextView tvSignUp  = findViewById(R.id.tvRegisterLink);

        if (btnGuest != null) {
            btnGuest.setOnClickListener(v -> {
                settingsManager.setLoggedIn(true);
                settingsManager.setGuestMode(true);
                settingsManager.saveUserProfile("Guest Profile", " ");
                Toast.makeText(this, "Continuing as Guest", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomepageActivity.class));
                finish();
            });
        }

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> handleEmailSignIn());
        }

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v ->
                    startActivity(new Intent(this, SignUpActivity.class)));
        }

        findViewById(R.id.btn_google).setOnClickListener(v -> handleGoogleSignIn());

        findViewById(R.id.btn_facebook).setOnClickListener(v ->
                Toast.makeText(this,
                        "Please integrate Facebook SDK for real authentication.",
                        Toast.LENGTH_LONG).show());

        findViewById(R.id.btn_x).setOnClickListener(v ->
                Toast.makeText(this,
                        "Please integrate X (Twitter) SDK for real authentication.",
                        Toast.LENGTH_LONG).show());

        // Permissions are handled by SplashActivity on first launch — nothing to do here.
    }

    private void handleEmailSignIn() {
        if (etEmail == null || etPassword == null) return;

        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toast.makeText(this,
                    "Password must be at least 10 characters, include a number " +
                            "and a special character.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        settingsManager.setLoggedIn(true);
        settingsManager.setGuestMode(false);

        String username = email.contains("@") ? email.split("@")[0] : email;
        settingsManager.saveUserProfile(username, email);

        Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomepageActivity.class));
        finish();
    }

    private void handleGoogleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this, request, null, mainExecutor,
                new androidx.credentials.CredentialManagerCallback<
                        GetCredentialResponse, GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignInSuccess(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Sign-in error: " + e.getMessage());
                        Toast.makeText(SignInActivity.this,
                                "Sign-in failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleSignInSuccess(GetCredentialResponse result) {
        Credential credential = result.getCredential();
        if (!(credential instanceof CustomCredential)) {
            Toast.makeText(this, "Unrecognized credential.", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomCredential custom = (CustomCredential) credential;
        if (!custom.getType().equals(
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            Toast.makeText(this, "Unexpected credential type.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            GoogleIdTokenCredential googleCred =
                    GoogleIdTokenCredential.createFrom(custom.getData());

            String email       = googleCred.getId();
            String displayName = googleCred.getDisplayName();
            String name        = displayName != null ? displayName : email.split("@")[0];

            settingsManager.setLoggedIn(true);
            settingsManager.setGuestMode(false);
            settingsManager.saveUserProfile(name, email);

            Log.d(TAG, "Google Sign-In Success. Email: " + email);
            Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomepageActivity.class));
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error parsing Google credential", e);
            Toast.makeText(this, "Error processing Google account.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}