package com.example.newsreader.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.newsreader.R;
import com.example.newsreader.SettingsActivity;
import com.example.newsreader.SettingsManager;
import com.example.newsreader.SignInActivity;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvLinkedStatus;
    private SettingsManager settingsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        settingsManager = new SettingsManager(requireContext());

        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvLinkedStatus = view.findViewById(R.id.tv_linked_status);
        View btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        View btnLogout = view.findViewById(R.id.btn_logout);

        // Load persisted user data
        loadUserData();

        // Check for Google Linking from Intent (if any)
        if (getActivity() != null && getActivity().getIntent() != null) {
            boolean isLinked = getActivity().getIntent().getBooleanExtra("IS_GOOGLE_LINKED", false);
            if (isLinked) {
                String googleEmail = getActivity().getIntent().getStringExtra("GOOGLE_EMAIL");
                if (googleEmail != null) {
                    tvProfileEmail.setText(googleEmail);
                    settingsManager.saveUserProfile(tvProfileName.getText().toString(), googleEmail);
                }
                if (tvLinkedStatus != null) {
                    tvLinkedStatus.setVisibility(View.VISIBLE);
                    tvLinkedStatus.setText("Linked with Google Account");
                }
                View cardLinkedStatus = view.findViewById(R.id.card_linked_status);
                if (cardLinkedStatus != null) {
                    cardLinkedStatus.setVisibility(View.VISIBLE);
                }
            }
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        View itemSettings = view.findViewById(R.id.item_settings_personalization);
        if (itemSettings != null) {
            itemSettings.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                settingsManager.setLoggedIn(false);
                settingsManager.clearUserProfile();
                Intent intent = new Intent(requireActivity(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        }

        int fontSize = settingsManager.getFontSize();
        applyFontSize(view, fontSize);

        // Update avatar initials based on the user name
        updateAvatarInitials(view);

        return view;
    }

    private void updateAvatarInitials(View view) {
        String name = settingsManager.getUserName();
        TextView tvInitials = view.findViewById(R.id.tv_avatar_initials);
        if (tvInitials != null && name != null && !name.trim().isEmpty()) {
            // Get first letters of first + last name
            String[] parts = name.trim().split("\\s+");
            String initials;
            if (parts.length >= 2) {
                initials = String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0);
            } else {
                initials = String.valueOf(parts[0].charAt(0));
            }
            tvInitials.setText(initials.toUpperCase());
        }
    }

    private void loadUserData() {
        if (tvProfileName != null) {
            tvProfileName.setText(settingsManager.getUserName());
        }
        if (tvProfileEmail != null) {
            tvProfileEmail.setText(settingsManager.getUserEmail());
        }
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = dialogView.findViewById(R.id.et_edit_name);
        EditText etEmail = dialogView.findViewById(R.id.et_edit_email);

        if (etName != null && etEmail != null) {
            etName.setText(tvProfileName.getText());
            etEmail.setText(tvProfileEmail.getText());
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    if (etName != null && etEmail != null) {
                        String newName = etName.getText().toString();
                        String newEmail = etEmail.getText().toString();
                        
                        // Persist changes
                        settingsManager.saveUserProfile(newName, newEmail);

                        tvProfileName.setText(newName);
                        tvProfileEmail.setText(newEmail);
                        if (getView() != null) {
                            updateAvatarInitials(getView());
                        }
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applyFontSize(View view, int fontSize) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                applyFontSize(group.getChildAt(i), fontSize);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTextSize(fontSize);
        }
    }
}