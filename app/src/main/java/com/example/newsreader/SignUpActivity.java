package com.example.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        SettingsManager settingsManager = new SettingsManager(this);

        EditText etName = findViewById(R.id.et_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnSignUp = findViewById(R.id.btn_sign_up);
        TextView tvSignIn = findViewById(R.id.tv_sign_in);

        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user profile and log in
            settingsManager.saveUserProfile(name, email);
            settingsManager.setLoggedIn(true);
            settingsManager.setGuestMode(false);

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

            // Redirect to Homepage
            Intent intent = new Intent(SignUpActivity.this, HomepageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        tvSignIn.setOnClickListener(v -> finish());
    }
}