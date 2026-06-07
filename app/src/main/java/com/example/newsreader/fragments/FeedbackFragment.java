package com.example.newsreader.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.newsreader.R;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeedbackFragment extends Fragment {

    private RatingBar ratingBar;
    private TextView tvRatingText;
    private TextInputEditText etMessage;
    private SwitchCompat switchAnonymous;
    private LinearLayout containerImages;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private List<Uri> selectedImages = new ArrayList<>();

    // ✅ Permission Launcher for Feedback specific needs
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            results -> {
                for (Map.Entry<String, Boolean> entry : results.entrySet()) {
                    if (!entry.getValue()) {
                        Log.w("FeedbackPermissions", "Denied: " + entry.getKey());
                    }
                }
            }
    );

    String[] ratingMessages = {
            "Tap a star to rate",
            "We'll do better!",
            "Sorry to hear that.",
            "Thanks for the feedback!",
            "Great! Glad you like it.",
            "Awesome! Thank you!"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Check if required permissions are granted, if not, ask again (as requested)
        checkAndRequestPermissions();

        // Modern Image Picker (No runtime permissions required!)
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImages.add(uri);
                        
                        // Dynamically add a small preview of the attached image
                        ImageView imageView = new ImageView(getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(160, 160);
                        params.setMargins(0, 0, 16, 0);
                        imageView.setLayoutParams(params);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setImageURI(uri);
                        
                        // Optional: Round the corners if needed, but simple for now
                        if (containerImages != null) {
                            containerImages.addView(imageView);
                        }

                        Toast.makeText(getContext(), "Image attached! (" + selectedImages.size() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        // Initialize Views
        ratingBar = view.findViewById(R.id.ratingBar);
        tvRatingText = view.findViewById(R.id.tv_rating_text);
        etMessage = view.findViewById(R.id.et_feedback_message);
        switchAnonymous = view.findViewById(R.id.switch_anonymous);
        containerImages = view.findViewById(R.id.container_images);

        MaterialButton btnAttach = view.findViewById(R.id.btn_attach_photos);
        MaterialButton btnSubmit = view.findViewById(R.id.btn_submit_feedback);
        MaterialButton btnGooglePlay = view.findViewById(R.id.btn_rate_google_play);
        View btnBack = view.findViewById(R.id.btn_back);

        // 0. Back Button Listener
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        // 1. Dynamic Rating Bar Listener with Unselect Logic
        ratingBar.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int numStars = ratingBar.getNumStars();
                float width = v.getWidth() - v.getPaddingLeft() - v.getPaddingRight();
                float x = event.getX() - v.getPaddingLeft();
                
                if (width <= 0) return false;

                float touchRating = (float) Math.ceil((x / width) * numStars);
                float currentRating = ratingBar.getRating();
                
                // If they tap the already selected star, unselect it (set to 0)
                if (touchRating > 0 && Math.abs(touchRating - currentRating) < 0.5f) {
                    ratingBar.setRating(0);
                    updateRatingUI(0);
                    v.performClick();
                    return true; // Consume touch to toggle off
                }
            }
            return false; // Let the RatingBar handle normal selection
        });

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                updateRatingUI(rating);
            }
        });

        // 2. Attach Photos (Launches modern system photo picker)
        btnAttach.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 3. Submit Feedback with Validation
        btnSubmit.setOnClickListener(v -> submitFeedback());

        // 4. Rate on Google Play
        btnGooglePlay.setOnClickListener(v -> {
            String packageName = requireContext().getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        });

        return view;
    }

    // Update text and colors based on rating
    private void updateRatingUI(float rating) {
        int index = (int) rating;
        if (index >= 0 && index < ratingMessages.length) {
            tvRatingText.setText(ratingMessages[index]);
        }

        // Change text color based on rating
        if (rating == 0) {
            tvRatingText.setTextColor(Color.parseColor("#757575")); // Grey
        } else if (rating >= 4) {
            tvRatingText.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (rating <= 2) {
            tvRatingText.setTextColor(Color.parseColor("#F44336")); // Red
        } else {
            tvRatingText.setTextColor(Color.parseColor("#757575")); // Grey
        }
    }

    // Professional Form Validation
    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String message = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        boolean isAnonymous = switchAnonymous.isChecked();

        // Check if user rated
        if (rating == 0) {
            Toast.makeText(getContext(), "Please rate your experience!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user typed a message
        if (message.isEmpty()) {
            Toast.makeText(getContext(), "Please tell us more about your experience.", Toast.LENGTH_SHORT).show();
            etMessage.requestFocus();
            return;
        }

        // TODO: Here you would typically send 'rating', 'message', 'isAnonymous', and 'selectedImages'
        // to your backend, Firebase, or email service (e.g., using JavaMail API).

        // Simulate successful submission
        Toast.makeText(getContext(), "Feedback submitted successfully! Thank you.", Toast.LENGTH_LONG).show();

        // Navigate back to the previous screen
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }

    // ✅ Check and request media permissions if they were previously denied
    private void checkAndRequestPermissions() {
        List<String> toRequest = new ArrayList<>();
        
        // Since minSdk is 34, we focus on images for feedback
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            toRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
        }

        if (!toRequest.isEmpty()) {
            permissionLauncher.launch(toRequest.toArray(new String[0]));
        }
    }
}
