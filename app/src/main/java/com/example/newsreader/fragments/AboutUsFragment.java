package com.example.newsreader.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.newsreader.NetworkUtils;
import com.example.newsreader.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AboutUsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        // 1. Setup Toolbar Back Button
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // 2. Dynamically set App Version
        TextView tvVersion = view.findViewById(R.id.tv_version);
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            if (tvVersion != null) {
                tvVersion.setText("Version " + pInfo.versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (tvVersion != null) {
                tvVersion.setText("Version 1.0.0");
            }
        }

        // 3. Social Links Click Listeners
        FloatingActionButton fabWebsite = view.findViewById(R.id.fab_website);
        FloatingActionButton fabInstagram = view.findViewById(R.id.fab_instagram);
        FloatingActionButton fabWhatsapp = view.findViewById(R.id.fab_whatsapp);
        FloatingActionButton fabTiktok = view.findViewById(R.id.fab_tiktok);
        FloatingActionButton fabEmail = view.findViewById(R.id.fab_email);

        if (fabWebsite != null) fabWebsite.setOnClickListener(v -> openUrl("https://www.your-website.com"));
        if (fabInstagram != null) fabInstagram.setOnClickListener(v -> openUrl("https://www.instagram.com/dcyph.r?igsh=aHpmNWI1OXhsMGY1"));
        if (fabWhatsapp != null) fabWhatsapp.setOnClickListener(v -> openUrl("https://wa.me/255623269331"));
        if (fabTiktok != null) fabTiktok.setOnClickListener(v -> openUrl("https://www.tiktok.com/@innohc7?_r=1&_t=ZS-96ys7vYW3gT"));

        if (fabEmail != null) {
            fabEmail.setOnClickListener(v -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:innocentnyali148@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "News Reader App Support");
                try {
                    startActivity(emailIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 3.1. Phone Contact Row Listeners
        View btnDial = view.findViewById(R.id.btn_dial);
        View btnSms = view.findViewById(R.id.btn_sms);
        View btnOnlineCaller = view.findViewById(R.id.btn_online_caller);
        String phoneNum = getString(R.string.phone_number);

        if (btnDial != null) {
            btnDial.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phoneNum));
                startActivity(dialIntent);
            });
        }

        if (btnSms != null) {
            btnSms.setOnClickListener(v -> {
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                smsIntent.setData(Uri.parse("smsto:" + phoneNum));
                startActivity(smsIntent);
            });
        }

        if (btnOnlineCaller != null) {
            btnOnlineCaller.setOnClickListener(v -> {
                if (NetworkUtils.isInternetAvailable(requireContext())) {
                    // Feature works only with internet
                    Toast.makeText(getContext(), "Starting Online Call to " + phoneNum + "...", Toast.LENGTH_LONG).show();
                    // Interface for online caller: This could be a Web-Call or VOIP implementation
                    // For now, we simulate the interface start
                } else {
                    Toast.makeText(getContext(), "Internet connection required for Online Caller", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 4. Legal Links Click Listeners
        View privacyPolicy = view.findViewById(R.id.tv_privacy_policy);
        if (privacyPolicy != null) {
            privacyPolicy.setOnClickListener(v -> openUrl("https://www.your-website.com/privacy-policy"));
        }

        View termsOfService = view.findViewById(R.id.tv_terms_of_service);
        if (termsOfService != null) {
            termsOfService.setOnClickListener(v -> openUrl("https://www.your-website.com/terms-of-service"));
        }

        return view;
    }

    // Helper method to open URLs safely
    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not open link", Toast.LENGTH_SHORT).show();
        }
    }
}
