package com.example.newsreader;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reuses the same XML layout — only the Java host class changes
        setContentView(R.layout.fragment_about_us);

        // 1. Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 2. App version
        TextView tvVersion = findViewById(R.id.tv_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("Version " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("Version 1.0.0");
        }

        // 3. Social links
        FloatingActionButton fabWebsite   = findViewById(R.id.fab_website);
        FloatingActionButton fabInstagram = findViewById(R.id.fab_instagram);
        FloatingActionButton fabWhatsapp  = findViewById(R.id.fab_whatsapp);
        FloatingActionButton fabTiktok    = findViewById(R.id.fab_tiktok);
        FloatingActionButton fabEmail     = findViewById(R.id.fab_email);

        fabWebsite.setOnClickListener(v ->
                openUrl("https://www.your-website.com"));
        fabInstagram.setOnClickListener(v ->
                openUrl("https://www.instagram.com/dcyph.r?igsh=aHpmNWI1OXhsMGY1"));
        fabWhatsapp.setOnClickListener(v ->
                openUrl("https://wa.me/255623269331"));
        fabTiktok.setOnClickListener(v ->
                openUrl("https://www.tiktok.com/@innohc7?_r=1&_t=ZS-96ys7vYW3gT"));

        fabEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:innocentnyali148@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "News Reader App Support");
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // 3.1. Phone row
        String phoneNum = getString(R.string.phone_number);

        findViewById(R.id.btn_dial).setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNum));
            startActivity(dialIntent);
        });

        findViewById(R.id.btn_sms).setOnClickListener(v -> {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phoneNum));
            startActivity(smsIntent);
        });

        findViewById(R.id.btn_online_caller).setOnClickListener(v -> {
            if (NetworkUtils.isInternetAvailable(this)) {
                Toast.makeText(this,
                        "Starting Online Call to " + phoneNum + "...",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                        "Internet connection required for Online Caller",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Legal links
        findViewById(R.id.tv_privacy_policy).setOnClickListener(v ->
                openUrl("https://www.your-website.com/privacy-policy"));

        findViewById(R.id.tv_terms_of_service).setOnClickListener(v ->
                openUrl("https://www.your-website.com/terms-of-service"));
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show();
        }
    }
}