package com.example.newsreader;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button btnSignUp = findViewById(R.id.btn_sign_up);
        TextView tvSignIn = findViewById(R.id.tv_sign_in);

        btnSignUp.setOnClickListener(v -> finish());
        tvSignIn.setOnClickListener(v -> finish());
    }
}