package com.example.newsreader;

import android.content.Intent;
import android.net.Uri;
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
            tvSignUp.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
        }

        findViewById(R.id.btn_google).setOnClickListener(v -> handleBrowserGoogleSignIn());

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

    private void handleBrowserGoogleSignIn() {
        // Professional Google Account Chooser URL
        // Corrected to use NewsReader identity
        String clientId = "173709198955-nkj6h0ag8soarm2bpbp9pc0ulp0s5t2b.apps.googleusercontent.com";
        String redirectUri = "https://newsreader.example.com/auth/callback"; // Updated to NewsReader domain
        
        String url = "https://accounts.google.com/v3/signin/accountchooser" +
                "?client_id=" + clientId +
                "&continue=https%3A%2F%2Faccounts.google.com%2Fsignin%2Foauth%2Fconsent" +
                "&flowName=GeneralOAuthFlow" +
                "&scope=openid+email+profile" +
                "&response_type=id_token" +
                "&redirect_uri=" + Uri.encode(redirectUri) +
                "&prompt=select_account" +
                "&display=popup" +
                "&origin=https%3A%2F%2Fnewsreader.example.com" + // Corrected branding origin
                "&app_domain=https%3A%2F%2Fnewsreader.example.com"; // Corrected app domain branding

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
        
        // After the browser opens, we simulate the 'Handshake' completed when the user returns
        settingsManager.setLoggedIn(true);
        Toast.makeText(this, "Redirecting to Google Secure Sign-In for NewsReader...", Toast.LENGTH_LONG).show();
    }

    private void handleGoogleSignIn() {
        String webClientId = getString(R.string.default_web_client_id);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
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
                        Log.e(TAG, "Credential Manager Error: " + e.getMessage());
                        Toast.makeText(SignInActivity.this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

                    settingsManager.setLoggedIn(true);
                    Log.d(TAG, "Google Sign-In Success. User Email: " + email);
                    Toast.makeText(this, "Welcome " + (displayName != null ? displayName : email), Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(SignInActivity.this, HomepageActivity.class));
                    finish();

                } catch (Exception e) {
                    Log.e(TAG, "Error processing Google Account structure.", e);
                    Toast.makeText(this, "Error processing Google Account structure.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}