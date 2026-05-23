package com.example.newsreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.newsreader.R;
import com.example.newsreader.SettingsManager;

public class FeedsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feeds, container, false);
        
        SettingsManager settingsManager = new SettingsManager(requireContext());
        int fontSize = settingsManager.getFontSize();
        
        applyFontSize(view, fontSize);
        
        return view;
    }

    private void applyFontSize(View view, int fontSize) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                applyFontSize(group.getChildAt(i), fontSize);
            }
        } else if (view instanceof TextView) {
            // Apply original sizing logic but respect Instagram-style header sizes
            if (view.getId() == R.id.toolbar_title) {
                 ((TextView) view).setTextSize(fontSize + 4);
            } else {
                 ((TextView) view).setTextSize(fontSize);
            }
        }
    }
}