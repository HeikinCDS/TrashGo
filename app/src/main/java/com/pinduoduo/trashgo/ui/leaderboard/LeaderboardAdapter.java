package com.pinduoduo.trashgo.ui.leaderboard;

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

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private final List<LeaderboardEntry> entries = new ArrayList<>();

    public void submitList(List<LeaderboardEntry> newList) {
        entries.clear();
        if (newList != null) {
            entries.addAll(newList);
        }
        notifyDataSetChanged();
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
        holder.bind(entries.get(position));
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

        public void bind(LeaderboardEntry entry) {
            binding.rankText.setText("#" + entry.getRank());
            binding.userName.setText(entry.getDisplayName());
            binding.avatarInitial.setText(entry.getAvatarInitial());
            binding.recycledText.setText(entry.getItemsRecycled() + " items recycled");
            binding.pointsText.setText(String.format("%,d pts", entry.getTotalPoints()));

            if (entry.isCurrentUser()) {
                binding.youTag.setVisibility(View.VISIBLE);
                binding.leaderboardCard.setStrokeColor(ContextCompat.getColor(
                        binding.getRoot().getContext(), R.color.trashgo_primary));
                binding.leaderboardCard.setStrokeWidth(3);
                binding.leaderboardCard.setCardBackgroundColor(ContextCompat.getColor(
                        binding.getRoot().getContext(), R.color.trashgo_primary_container_soft));
            } else {
                binding.youTag.setVisibility(View.GONE);
                binding.leaderboardCard.setStrokeColor(ContextCompat.getColor(
                        binding.getRoot().getContext(), R.color.trashgo_divider));
                binding.leaderboardCard.setStrokeWidth(1);
                binding.leaderboardCard.setCardBackgroundColor(ContextCompat.getColor(
                        binding.getRoot().getContext(), R.color.trashgo_surface));
            }
        }
    }
}
