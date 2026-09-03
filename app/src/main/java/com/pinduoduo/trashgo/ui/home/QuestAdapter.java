package com.pinduoduo.trashgo.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.Quest;
import com.pinduoduo.trashgo.databinding.ItemQuestBinding;

import java.util.ArrayList;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

    private final List<Quest> quests = new ArrayList<>();

    public void submitList(List<Quest> newList) {
        quests.clear();
        if (newList != null) {
            quests.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuestBinding binding = ItemQuestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new QuestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        holder.bind(quests.get(position));
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {
        private final ItemQuestBinding binding;

        public QuestViewHolder(@NonNull ItemQuestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Quest quest) {
            binding.questTitle.setText(quest.getTitle());
            binding.questReward.setText("+" + quest.getRewardPoints() + " Pts");

            if (quest.isObjectiveOfDay()) {
                binding.questTag.setText("🌟 Objective of the Day");
                binding.questCard.setStrokeColor(ContextCompat.getColor(
                        binding.getRoot().getContext(), R.color.trashgo_primary));
                binding.questCard.setStrokeWidth(3);
            } else {
                binding.questTag.setText("🎯 Side Quest");
                binding.questCard.setStrokeColor(ContextCompat.getColor(
                        binding.getRoot().getContext(), R.color.trashgo_divider));
                binding.questCard.setStrokeWidth(1);
            }

            int current = quest.getCurrentAmount();
            int target = quest.getTargetAmount();
            binding.questProgressBar.setMax(target);
            binding.questProgressBar.setProgress(Math.min(current, target));

            if (quest.isCompleted()) {
                binding.questProgressText.setText("Completed! ✅");
                binding.questProgressText.setTextColor(ContextCompat.getColor(
                        binding.getRoot().getContext(), R.color.trashgo_primary));
            } else {
                binding.questProgressText.setText(current + " / " + target);
                binding.questProgressText.setTextColor(ContextCompat.getColor(
                        binding.getRoot().getContext(), R.color.trashgo_on_surface_variant));
            }
        }
    }
}
