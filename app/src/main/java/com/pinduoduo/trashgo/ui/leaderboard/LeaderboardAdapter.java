package com.pinduoduo.trashgo.ui.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.LeaderboardEntry;
import com.pinduoduo.trashgo.databinding.ItemLeaderboardBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    private final List<LeaderboardEntry> entries = new ArrayList<>();
    private long topPoints;

    public void submitList(List<LeaderboardEntry> newList, long topPoints) {
        this.topPoints = topPoints;
        entries.clear();
        if (newList != null) {
            entries.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public void submitList(List<LeaderboardEntry> newList) {
        long top = 0L;
        if (newList != null && !newList.isEmpty()) {
            top = newList.get(0).getTotalPoints();
        }
        submitList(newList, top);
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardBinding binding = ItemLeaderboardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LeaderboardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        holder.bind(entries.get(position), position == entries.size() - 1, topPoints);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private final ItemLeaderboardBinding binding;

        public LeaderboardViewHolder(@NonNull ItemLeaderboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LeaderboardEntry entry, boolean isLast, long topPoints) {
            Context context = binding.getRoot().getContext();

            binding.rankText.setText(String.valueOf(entry.getRank()));
            binding.userName.setText(entry.getDisplayName());
            binding.avatarInitial.setText(entry.getAvatarInitial());
            binding.recycledText.setText(context.getString(
                    R.string.leaderboard_items_recycled, entry.getItemsRecycled()));
            binding.pointsText.setText(String.format(Locale.US, "%,d", entry.getTotalPoints()));

            binding.rankText.setTextColor(ContextCompat.getColor(context,
                    entry.getRank() <= 3 ? R.color.trashgo_on_surface : R.color.trashgo_on_surface_faint));

            int share = 0;
            if (topPoints > 0L && entry.getTotalPoints() > 0L) {
                share = (int) Math.round(entry.getTotalPoints() * 100.0d / topPoints);
                if (share < 2) share = 2;
                if (share > 100) share = 100;
            }
            binding.shareBar.setProgress(share);

            if (entry.isCurrentUser()) {
                binding.youTag.setVisibility(View.VISIBLE);
                binding.leaderboardRow.setBackgroundColor(ContextCompat.getColor(
                        context, R.color.trashgo_primary_container_soft));
            } else {
                binding.youTag.setVisibility(View.GONE);
                binding.leaderboardRow.setBackgroundColor(ContextCompat.getColor(
                        context, R.color.trashgo_surface));
            }

            binding.leaderboardDivider.setVisibility(isLast ? View.GONE : View.VISIBLE);
        }
    }
}
