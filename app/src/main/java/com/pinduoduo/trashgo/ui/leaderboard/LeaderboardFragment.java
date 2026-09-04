package com.pinduoduo.trashgo.ui.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.LeaderboardEntry;
import com.pinduoduo.trashgo.databinding.FragmentLeaderboardBinding;
import com.pinduoduo.trashgo.util.PointTotals;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeaderboardFragment extends Fragment {
    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;
    private ListenerRegistration leaderboardListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new LeaderboardAdapter();
        binding.leaderboardList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.leaderboardList.setAdapter(adapter);
        binding.leaderboardList.setHasFixedSize(false);
        showLoading();
    }

    private void showLoading() {
        if (binding == null) return;
        binding.leaderboardEmpty.setVisibility(View.GONE);
        binding.leaderboardPodium.setVisibility(View.GONE);
        binding.leaderboardYouCard.setVisibility(View.GONE);
        binding.leaderboardRankingLabel.setVisibility(View.GONE);
        binding.leaderboardRankingDivider.setVisibility(View.GONE);
        binding.leaderboardList.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (leaderboardListener != null || binding == null) return;
        final FragmentLeaderboardBinding currentBinding = binding;

        leaderboardListener = FirebaseFirestore.getInstance().collection("users")
                .addSnapshotListener((snapshot, error) -> {
                    if (binding != currentBinding || !isAdded()) return;
                    if (error != null) {
                        Toast.makeText(requireContext(), "Unable to update leaderboard. Please reopen to retry.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot == null) return;
                    String currentUid = FirebaseAuth.getInstance().getUid();
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("displayName");
                        if (name == null || name.trim().isEmpty()) name = doc.getString("name");
                        if (name == null || name.trim().isEmpty()) name = "TrashGo User";
                        long earned = PointTotals.lifetime(doc.getLong("lifetimePoints"),
                                doc.getLong("totalPoints"));
                        entries.add(new LeaderboardEntry(doc.getId(), name, earned,
                                PointTotals.balance(doc.getLong("itemsRecycled")),
                                doc.getId().equals(currentUid), false));
                    }
                    entries.sort((left, right) -> {
                        int score = Long.compare(right.getTotalPoints(), left.getTotalPoints());
                        return score != 0 ? score : left.getUid().compareTo(right.getUid());
                    });
                    if (entries.size() > 50) entries = new ArrayList<>(entries.subList(0, 50));
                    for (int i = 0; i < entries.size(); i++) entries.get(i).setRank(i + 1);
                    render(entries);
                });
    }

    private void render(List<LeaderboardEntry> entries) {
        if (binding == null || adapter == null) return;

        boolean empty = entries.isEmpty();
        binding.leaderboardEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.leaderboardPodium.setVisibility(empty ? View.GONE : View.VISIBLE);

        bindPodium(entries);
        bindYourPosition(entries);

        List<LeaderboardEntry> rest = entries.size() > 3
                ? new ArrayList<>(entries.subList(3, entries.size()))
                : new ArrayList<>();
        boolean hasRest = !rest.isEmpty();
        binding.leaderboardRankingLabel.setVisibility(hasRest ? View.VISIBLE : View.GONE);
        binding.leaderboardRankingDivider.setVisibility(hasRest ? View.VISIBLE : View.GONE);
        binding.leaderboardList.setVisibility(hasRest ? View.VISIBLE : View.GONE);

        long topPoints = empty ? 0L : entries.get(0).getTotalPoints();
        adapter.submitList(rest, topPoints);
    }

    private void bindPodium(List<LeaderboardEntry> entries) {
        bindPodiumSlot(entries, 0, binding.podiumOne, binding.podiumOneInitial,
                binding.podiumOneName, binding.podiumOnePoints);
        bindPodiumSlot(entries, 1, binding.podiumTwo, binding.podiumTwoInitial,
                binding.podiumTwoName, binding.podiumTwoPoints);
        bindPodiumSlot(entries, 2, binding.podiumThree, binding.podiumThreeInitial,
                binding.podiumThreeName, binding.podiumThreePoints);
    }

    private void bindPodiumSlot(List<LeaderboardEntry> entries, int index, View column,
                                TextView initial, TextView name, TextView points) {
        if (index >= entries.size()) {
            column.setVisibility(View.INVISIBLE);
            return;
        }
        LeaderboardEntry entry = entries.get(index);
        column.setVisibility(View.VISIBLE);
        initial.setText(entry.getAvatarInitial());
        name.setText(entry.getDisplayName());
        name.setTextColor(ContextCompat.getColor(requireContext(), entry.isCurrentUser()
                ? R.color.trashgo_primary
                : R.color.trashgo_on_surface));
        points.setText(String.format(Locale.US, "%,d", entry.getTotalPoints()));
    }

    private void bindYourPosition(List<LeaderboardEntry> entries) {
        int me = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).isCurrentUser()) {
                me = i;
                break;
            }
        }
        if (me < 0) {
            binding.leaderboardYouCard.setVisibility(View.GONE);
            return;
        }

        LeaderboardEntry mine = entries.get(me);
        binding.leaderboardYouCard.setVisibility(View.VISIBLE);
        binding.leaderboardYouRank.setText(getString(R.string.leaderboard_rank_format, mine.getRank()));
        binding.leaderboardYouPoints.setText(String.format(Locale.US, "%,d", mine.getTotalPoints()));

        if (me == 0) {
            binding.leaderboardYouGap.setText(R.string.leaderboard_leading);
            return;
        }

        LeaderboardEntry above = entries.get(me - 1);
        long gap = above.getTotalPoints() - mine.getTotalPoints();
        if (gap <= 0L) {
            binding.leaderboardYouGap.setText(getString(R.string.leaderboard_gap_tied, above.getRank()));
        } else {
            binding.leaderboardYouGap.setText(getString(R.string.leaderboard_gap_behind,
                    String.format(Locale.US, "%,d", gap), above.getRank()));
        }
    }

    private void stopListening() {
        if (leaderboardListener != null) {
            leaderboardListener.remove();
            leaderboardListener = null;
        }
    }

    @Override
    public void onStop() {
        stopListening();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        stopListening();
        binding.leaderboardList.setAdapter(null);
        binding = null;
        adapter = null;
        super.onDestroyView();
    }
}
