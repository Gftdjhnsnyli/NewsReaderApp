package com.example.newsreader.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.newsreader.R;
import com.example.newsreader.SettingsActivity;
import com.example.newsreader.SettingsManager;
import com.example.newsreader.SignInActivity;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private ImageView ivProfileAvatar;
    private TextView tvAvatarInitials;
    private SettingsManager settingsManager;

    private Uri tempImageUri;
    private String selectedAvatarUri; // Temporary storage for dialog selection
    private ImageView ivDialogAvatar;
    private TextView tvDialogInitials;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    onAvatarSelected(uri);
                }
            });

    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result && tempImageUri != null) {
                    onAvatarSelected(tempImageUri);
                }
            });

    private void onAvatarSelected(Uri uri) {
        selectedAvatarUri = uri.toString();
        if (ivDialogAvatar != null) {
            ivDialogAvatar.setPadding(0, 0, 0, 0);
            Glide.with(this).load(uri).circleCrop().into(ivDialogAvatar);
            if (tvDialogInitials != null) tvDialogInitials.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        settingsManager = new SettingsManager(requireContext());

        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar);
        tvAvatarInitials = view.findViewById(R.id.tv_avatar_initials);

        TextView tvLinkedStatus = view.findViewById(R.id.tv_linked_status);
        View cardLinkedStatus = view.findViewById(R.id.card_linked_status);
        View btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);

        // Load persisted user data
        loadUserData();

        // Check for Google Linking from Intent
        if (getActivity() != null && getActivity().getIntent() != null) {
            boolean isLinked = getActivity().getIntent().getBooleanExtra("IS_GOOGLE_LINKED", false);
            if (isLinked) {
                String googleEmail = getActivity().getIntent().getStringExtra("GOOGLE_EMAIL");
                if (googleEmail != null) {
                    tvProfileEmail.setText(googleEmail);
                    settingsManager.saveUserProfile(settingsManager.getUserName(), googleEmail);
                }
                if (tvLinkedStatus != null) {
                    tvLinkedStatus.setVisibility(View.VISIBLE);
                    tvLinkedStatus.setText(R.string.linked_with_google);
                }
                if (cardLinkedStatus != null) {
                    cardLinkedStatus.setVisibility(View.VISIBLE);
                }
            }
        }

        if (settingsManager.isGuestMode()) {
            // GUEST UI
            tvProfileName.setText("Guest Profile");
            tvProfileEmail.setText("Browse as guest to access news features.");

            if (btnLogout != null) {
                btnLogout.setText(R.string.sign_in);
                btnLogout.setOnClickListener(v -> {
                    settingsManager.setGuestMode(false);
                    Intent intent = new Intent(requireContext(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }
            if (btnEditProfile != null) {
                btnEditProfile.setVisibility(View.GONE);
            }
        } else {
            // NORMAL USER UI
            if (btnLogout != null) {
                btnLogout.setText(R.string.sign_out);
                btnLogout.setOnClickListener(v -> {
                    settingsManager.setLoggedIn(false);
                    settingsManager.setGuestMode(false);
                    settingsManager.clearUserProfile();

                    Intent intent = new Intent(requireContext(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                });
            }
        }

        if (btnEditProfile != null && !settingsManager.isGuestMode()) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        View itemSettings = view.findViewById(R.id.item_settings_personalization);
        if (itemSettings != null) {
            itemSettings.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                startActivity(intent);
            });
        }

        View itemAboutUs = view.findViewById(R.id.item_about_us);
        if (itemAboutUs != null) {
            itemAboutUs.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AboutUsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        int fontSize = settingsManager.getFontSize();
        applyFontSize(view, fontSize);
        loadAvatar(view);

        return view;
    }

    private void loadAvatar(View view) {
        if (view == null) return;
        String avatarUri = settingsManager.getUserAvatar();
        if (avatarUri != null && !avatarUri.isEmpty()) {
            ivProfileAvatar.setVisibility(View.VISIBLE);
            tvAvatarInitials.setVisibility(View.GONE);
            Glide.with(this).load(Uri.parse(avatarUri)).circleCrop().into(ivProfileAvatar);
        } else {
            ivProfileAvatar.setVisibility(View.GONE);
            tvAvatarInitials.setVisibility(View.VISIBLE);
            updateAvatarInitials(view);
        }
    }

    private void updateAvatarInitials(View view) {
        if (view == null) return;
        String name = settingsManager.isGuestMode() ? "Guest" : settingsManager.getUserName();
        TextView tvInitials = view.findViewById(R.id.tv_avatar_initials);
        if (tvInitials != null && name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            String initials = (parts.length >= 2)
                ? "" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)
                : "" + parts[0].charAt(0);
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
        ivDialogAvatar = dialogView.findViewById(R.id.iv_edit_avatar);
        tvDialogInitials = dialogView.findViewById(R.id.tv_edit_avatar_initials);
        View cardAvatar = dialogView.findViewById(R.id.card_edit_avatar);

        selectedAvatarUri = settingsManager.getUserAvatar();
        
        if (etName != null && etEmail != null) {
            etName.setText(tvProfileName.getText());
            etEmail.setText(tvProfileEmail.getText());
        }

        if (selectedAvatarUri != null) {
            ivDialogAvatar.setPadding(0, 0, 0, 0);
            Glide.with(this).load(Uri.parse(selectedAvatarUri)).circleCrop().into(ivDialogAvatar);
            tvDialogInitials.setVisibility(View.GONE);
        } else {
            tvDialogInitials.setVisibility(View.VISIBLE);
            String name = etName != null ? etName.getText().toString() : "JD";
            if (!name.isEmpty()) {
                tvDialogInitials.setText(String.valueOf(name.charAt(0)).toUpperCase());
            }
        }

        if (cardAvatar != null) {
            cardAvatar.setOnClickListener(v -> showImageSourceOptions());
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.save_settings, (dialog, which) -> {
                    if (etName != null && etEmail != null) {
                        String newName = etName.getText().toString();
                        String newEmail = etEmail.getText().toString();
                        settingsManager.saveUserProfile(newName, newEmail);
                        settingsManager.setUserAvatar(selectedAvatarUri);
                        
                        tvProfileName.setText(newName);
                        tvProfileEmail.setText(newEmail);
                        loadAvatar(getView());
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showImageSourceOptions() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Avatar Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openGallery() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
            return;
        }

        try {
            File photoFile = createImageFile();
            tempImageUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePicture.launch(tempImageUri);
        } catch (IOException ex) {
            Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
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