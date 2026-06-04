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
import androidx.credentials.CustomCredential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        settingsManager = new SettingsManager(this);
        credentialManager = CredentialManager.create(this);
        mainExecutor = ContextCompat.getMainExecutor(this);

        // Initialize EditText fields
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        Button btnSignIn = findViewById(R.id.btnLogin);
        Button btnGuest = findViewById(R.id.btnGuest);
        TextView tvSignUp = findViewById(R.id.tvRegisterLink);

        if (btnGuest != null){
            btnGuest.setOnClickListener(v ->{
                settingsManager.setLoggedIn(true);
                settingsManager.setGuestMode(true);
                settingsManager.saveUserProfile("Guest Profile", " ");

                Toast.makeText(this, "Continuing as Guest", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignInActivity.this, HomepageActivity.class));
                finish();
            });
        }

        if (btnSignIn != null) {
            //  credential validation
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

        // 4. TODO: Verify credentials against your backend or local database here.
        // For demonstration, if it passes validation, we log them in and save their actual profile.
        // This removes the default "John Doe" auto-account behavior.
        settingsManager.setLoggedIn(true);

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
}