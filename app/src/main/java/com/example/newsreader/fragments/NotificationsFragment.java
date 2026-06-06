package com.example.newsreader.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsreader.R;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Navigation for filters
        view.findViewById(R.id.btn_filter_offline).setOnClickListener(v -> 
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OfflineNewsFragment())
                .addToBackStack(null)
                .commit()
        );

        view.findViewById(R.id.btn_filter_bookmark).setOnClickListener(v -> 
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new BookmarksFragment())
                .addToBackStack(null)
                .commit()
        );

        RecyclerView rv = view.findViewById(R.id.rv_notifications);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<NotificationItem> data = new ArrayList<>();
        // Group: Today
        data.add(new NotificationItem("Meghan", "is follow you", "2 min ago", true, getString(R.string.follow_plus), 0, getString(R.string.today)));
        data.add(new NotificationItem("James", "has posted new politics news <b>\"Top 10 incredible in the world\"</b>", "4 hours ago", false, null, R.drawable.splash_glow, null));
        data.add(new NotificationItem("Armani", "has bookmarked your news <b>\"Top 10 incredible in the world\"</b> to <b>Favorite</b> collection", "8 hours ago", false, null, 0, null));
        data.add(new NotificationItem("Dawson", "reply to your comment <b>\"Cool things!\"</b>", "23 hours ago", false, null, R.drawable.splash_glow, null));
        
        // Group: This week
        data.add(new NotificationItem("Darnell", "has comment your news <b>\"What nice idea\"</b>", "10/21/2021", false, null, R.drawable.splash_glow, getString(R.string.this_week)));
        data.add(new NotificationItem("Morrow", "has follow you back", "10/21/2021", true, getString(R.string.followed), 0, null));

        rv.setAdapter(new NotificationsAdapter(data));

        TextView tvCount = view.findViewById(R.id.tv_notification_count);
        if (tvCount != null) {
            tvCount.setText(String.format(Locale.getDefault(), getString(R.string.notification_count_template), 4));
        }

        return view;
    }

    private static class NotificationItem {
        String name, action, time, btnText, section;
        boolean hasButton;
        int thumbnailRes;

        NotificationItem(String name, String action, String time, boolean hasButton, String btnText, int thumbnailRes, String section) {
            this.name = name;
            this.action = action;
            this.time = time;
            this.hasButton = hasButton;
            this.btnText = btnText;
            this.thumbnailRes = thumbnailRes;
            this.section = section;
        }
    }

    private static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {
        List<NotificationItem> items;

        NotificationsAdapter(List<NotificationItem> items) { this.items = items; }

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
                
                // Styling based on text to match design (Red for Follow, Grey for Followed)
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
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvSection, tvText, tvTime;
            ImageView ivAvatar, ivThumb;
            MaterialButton btnAction;
            View cardThumb;

            VH(View v) {
                super(v);
                tvSection = v.findViewById(R.id.tv_section_header);
                tvText = v.findViewById(R.id.tv_notification_text);
                tvTime = v.findViewById(R.id.tv_time);
                ivAvatar = v.findViewById(R.id.iv_avatar);
                ivThumb = v.findViewById(R.id.iv_thumbnail);
                btnAction = v.findViewById(R.id.btn_action);
                cardThumb = v.findViewById(R.id.card_thumbnail);
            }
        }
    }
}
