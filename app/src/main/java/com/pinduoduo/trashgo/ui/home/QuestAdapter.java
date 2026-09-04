package com.pinduoduo.trashgo.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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
            Context context = binding.getRoot().getContext();

            binding.questTitle.setText(quest.getTitle());
            binding.questReward.setText("+" + quest.getRewardPoints());

            if (quest.isObjectiveOfDay()) {
                binding.questTag.setText(R.string.quest_objective_tag);
                binding.questCard.setStrokeColor(
                        ContextCompat.getColor(context, R.color.trashgo_primary));
                binding.questCard.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.trashgo_primary_container_soft));
            } else {
                binding.questTag.setText(R.string.quest_side_tag);
                binding.questCard.setStrokeColor(
                        ContextCompat.getColor(context, R.color.trashgo_divider));
                binding.questCard.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.trashgo_surface));
            }
            binding.questCard.setStrokeWidth(
                    Math.round(context.getResources().getDisplayMetrics().density));

            int current = quest.getCurrentAmount();
            int target = Math.max(1, quest.getTargetAmount());
            binding.questProgressBar.setMax(target);
            binding.questProgressBar.setProgress(Math.min(current, target));

            if (quest.isCompleted()) {
                binding.questProgressBar.setProgress(target);
                binding.questCheck.setVisibility(View.VISIBLE);
                binding.questProgressText.setText(R.string.quest_completed);
                binding.questProgressText.setTextColor(
                        ContextCompat.getColor(context, R.color.trashgo_primary));
            } else {
                binding.questCheck.setVisibility(View.GONE);
                binding.questProgressText.setText(
                        context.getString(R.string.quest_progress, current, target));
                binding.questProgressText.setTextColor(
                        ContextCompat.getColor(context, R.color.trashgo_on_surface_faint));
            }
        }
    }
}
