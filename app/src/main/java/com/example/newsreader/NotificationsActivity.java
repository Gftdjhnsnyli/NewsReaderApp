package com.example.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationsActivity extends AppCompatActivity {

    private List<NotificationItem> data;
    private NotificationsAdapter adapter;
    private RecyclerView rv;
    private View layoutEmptyState;
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SettingsManager settingsManager = new SettingsManager(this);
        settingsManager.applyLanguage(settingsManager.getAppLanguage());
        
        setContentView(R.layout.activity_notifications);

        // 1. Edge-to-Edge Setup
        androidx.activity.EdgeToEdge.enable(this);
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 2. Back Button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 3. Notification Count & Empty State
        tvCount = findViewById(R.id.tv_notification_count);
        layoutEmptyState = findViewById(R.id.layout_empty_state);

        // 4. Delete All Logic
        findViewById(R.id.btn_delete_all).setOnClickListener(v -> {
            if (data != null && !data.isEmpty()) {
                data.clear();
                adapter.notifyDataSetChanged();
                updateUI();
                Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Filter Chips Logic
        ChipGroup chipGroup = findViewById(R.id.chip_group_filters);
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    // Activities use Intent for navigation
                    if (checkedId == R.id.chip_offline) {
                         // Note: If you want to keep them as fragments, 
                         // you might need to handle navigation differently.
                         // For now, let's assume we return to Home and switch tabs?
                         // Or open as separate Activities if they exist.
                         Toast.makeText(this, "Navigating to Offline News...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // 6. RecyclerView Setup
        rv = findViewById(R.id.rv_notifications);
        rv.setLayoutManager(new LinearLayoutManager(this));

        data = new ArrayList<>();
       /* // Group: Today
        data.add(new NotificationItem("Meghan", "is following you", "2 min ago", true, getString(R.string.follow_plus), 0, getString(R.string.today)));
        data.add(new NotificationItem("James", "has posted new politics news <b>\"Top 10 incredible in the world\"</b>", "4 hours ago", false, null, R.drawable.splash_glow, null));
        data.add(new NotificationItem("Armani", "has bookmarked your news <b>\"Top 10 incredible in the world\"</b> to <b>Favorite</b> collection", "8 hours ago", false, null, 0, null));
        data.add(new NotificationItem("Dawson", "replied to your comment <b>\"Cool things!\"</b>", "23 hours ago", false, null, R.drawable.splash_glow, null));

        // Group: This week
        data.add(new NotificationItem("Darnell", "has commented your news <b>\"What nice idea\"</b>", "10/21/2021", false, null, R.drawable.splash_glow, getString(R.string.this_week)));
        data.add(new NotificationItem("Morrow", "has followed you back", "10/21/2021", true, getString(R.string.followed), 0, null));

        */

        adapter = new NotificationsAdapter(data, this::updateUI);
        rv.setAdapter(adapter);

        updateUI();
    }

    private void updateUI() {
        if (data == null || data.isEmpty()) {
            rv.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (tvCount != null) tvCount.setText(String.format(Locale.getDefault(), getString(R.string.notification_count_template), 0));
        } else {
            rv.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (tvCount != null) {
                tvCount.setText(String.format(Locale.getDefault(), getString(R.string.notification_count_template), data.size()));
            }
        }
    }

    private static class NotificationItem {
        String name, action, time, btnText, section;
        boolean hasButton;
        int thumbnailRes;

        NotificationItem(String name, String action, String time, boolean hasButton, String btnText, int thumbnailRes, String section) {
            this.name = name; this.action = action; this.time = time;
            this.hasButton = hasButton; this.btnText = btnText;
            this.thumbnailRes = thumbnailRes; this.section = section;
        }
    }

    private static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {
        List<NotificationItem> items;
        Runnable onUpdate;

        NotificationsAdapter(List<NotificationItem> items, Runnable onUpdate) {
            this.items = items; this.onUpdate = onUpdate;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            NotificationItem item = items.get(position);

            if (item.section != null) {
                holder.tvSection.setVisibility(View.VISIBLE);
                holder.tvSection.setText(item.section);
            } else {
                holder.tvSection.setVisibility(View.GONE);
            }

            String fullText = "<b>" + item.name + "</b> " + item.action;
            holder.tvText.setText(Html.fromHtml(fullText, Html.FROM_HTML_MODE_COMPACT));
            holder.tvTime.setText(item.time);

            if (item.hasButton) {
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText(item.btnText);
                if (item.btnText.toLowerCase().contains("followed")) {
                    holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFEEEEF2));
                    holder.btnAction.setTextColor(0xFF757575);
                } else {
                    holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF4B55));
                    holder.btnAction.setTextColor(0xFFFFFFFF);
                }
                holder.cardThumb.setVisibility(View.GONE);
            } else if (item.thumbnailRes != 0) {
                holder.btnAction.setVisibility(View.GONE);
                holder.cardThumb.setVisibility(View.VISIBLE);
                holder.ivThumb.setImageResource(item.thumbnailRes);
            } else {
                holder.btnAction.setVisibility(View.GONE);
                holder.cardThumb.setVisibility(View.GONE);
            }

            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, items.size());
                    if (onUpdate != null) onUpdate.run();
                }
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvSection, tvText, tvTime;
            ImageView ivAvatar, ivThumb;
            MaterialButton btnAction;
            View cardThumb, btnDelete;

            VH(View v) {
                super(v);
                tvSection = v.findViewById(R.id.tv_section_header);
                tvText = v.findViewById(R.id.tv_notification_text);
                tvTime = v.findViewById(R.id.tv_time);
                ivAvatar = v.findViewById(R.id.iv_avatar);
                ivThumb = v.findViewById(R.id.iv_thumbnail);
                btnAction = v.findViewById(R.id.btn_action);
                cardThumb = v.findViewById(R.id.card_thumbnail);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}
