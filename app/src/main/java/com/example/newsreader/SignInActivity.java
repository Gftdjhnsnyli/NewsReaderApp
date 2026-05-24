package com.example.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    // ✅ REPLACE THIS with your Web Application Client ID from Google Cloud Console
    private static final String WEB_CLIENT_ID = "197897894392-s0bm5fn7mmvk07a19q86frthnh179mda.apps.googleusercontent.com";

    private CredentialManager credentialManager;
    private Executor mainExecutor;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        settingsManager = new SettingsManager(this);
        credentialManager = CredentialManager.create(this);
        mainExecutor = ContextCompat.getMainExecutor(this);

        Button btnSignIn = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvRegisterLink);

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                settingsManager.setLoggedIn(true);
                startActivity(new Intent(SignInActivity.this, HomepageActivity.class));
                finish();
            });
        }

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v ->
                    startActivity(new Intent(SignInActivity.this, SignUpActivity.class))
            );
        }

        // ✅ Now correctly calls the real Google Sign-In method
        findViewById(R.id.btn_google).setOnClickListener(v -> handleGoogleSignIn());

        findViewById(R.id.btn_facebook).setOnClickListener(v -> {
            settingsManager.setLoggedIn(true);
            Toast.makeText(this, "Signing in with Facebook...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, HomepageActivity.class));
            finish();
        });

        findViewById(R.id.btn_x).setOnClickListener(v -> {
            settingsManager.setLoggedIn(true);
            Toast.makeText(this, "Signing in with X...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, HomepageActivity.class));
            finish();
        });
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
                    String name = displayName != null ? displayName : "Google User";

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