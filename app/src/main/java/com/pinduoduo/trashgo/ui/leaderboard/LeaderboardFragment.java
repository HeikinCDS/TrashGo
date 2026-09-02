package com.pinduoduo.trashgo.ui.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinduoduo.trashgo.data.model.LeaderboardEntry;
import com.pinduoduo.trashgo.databinding.FragmentLeaderboardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;

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

        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        List<LeaderboardEntry> list = generateBotAccounts();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            processAndDisplayLeaderboard(list);
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (binding == null) return;
                    long userPoints = 0;
                    long userItems = 0;
                    String userName = currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()
                            ? currentUser.getDisplayName() : "You";

                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.contains("totalPoints") && doc.getLong("totalPoints") != null) {
                            userPoints = doc.getLong("totalPoints");
                        }
                        if (doc.contains("itemsRecycled") && doc.getLong("itemsRecycled") != null) {
                            userItems = doc.getLong("itemsRecycled");
                        }
                        if (doc.contains("displayName") && doc.getString("displayName") != null) {
                            String name = doc.getString("displayName");
                            if (name != null && !name.trim().isEmpty()) {
                                userName = name;
                            }
                        }
                    }

                    LeaderboardEntry userEntry = new LeaderboardEntry(
                            currentUser.getUid(),
                            userName,
                            userPoints,
                            userItems,
                            true,
                            false
                    );

                    list.add(userEntry);
                    processAndDisplayLeaderboard(list);
                });
    }

    private void processAndDisplayLeaderboard(List<LeaderboardEntry> list) {
        // Sort descending by totalPoints
        Collections.sort(list, (a, b) -> Long.compare(b.getTotalPoints(), a.getTotalPoints()));

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }

        if (binding == null) return;

        // Populate Top 3 Podium
        if (list.size() >= 1) {
            LeaderboardEntry first = list.get(0);
            binding.podium1stName.setText(first.isCurrentUser() ? "YOU (" + first.getDisplayName() + ")" : first.getDisplayName());
            binding.podium1stPoints.setText(String.format("%,d pts", first.getTotalPoints()));
        }
        if (list.size() >= 2) {
            LeaderboardEntry second = list.get(1);
            binding.podium2ndName.setText(second.isCurrentUser() ? "YOU (" + second.getDisplayName() + ")" : second.getDisplayName());
            binding.podium2ndPoints.setText(String.format("%,d pts", second.getTotalPoints()));
        }
        if (list.size() >= 3) {
            LeaderboardEntry third = list.get(2);
            binding.podium3rdName.setText(third.isCurrentUser() ? "YOU (" + third.getDisplayName() + ")" : third.getDisplayName());
            binding.podium3rdPoints.setText(String.format("%,d pts", third.getTotalPoints()));
        }

        // Submit complete rankings list to RecyclerView
        adapter.submitList(list);
    }

    private List<LeaderboardEntry> generateBotAccounts() {
        List<LeaderboardEntry> bots = new ArrayList<>();
        bots.add(new LeaderboardEntry("bot_1", "EcoMaster_99", 1450, 182, false, true));
        bots.add(new LeaderboardEntry("bot_2", "RecycleQueen", 1200, 145, false, true));
        bots.add(new LeaderboardEntry("bot_3", "GreenSam", 950, 110, false, true));
        bots.add(new LeaderboardEntry("bot_4", "TerraSaver", 780, 92, false, true));
        bots.add(new LeaderboardEntry("bot_5", "ZeroWasteHero", 600, 75, false, true));
        bots.add(new LeaderboardEntry("bot_6", "PlanetDefender", 420, 50, false, true));
        bots.add(new LeaderboardEntry("bot_7", "BioWarrior", 250, 30, false, true));
        bots.add(new LeaderboardEntry("bot_8", "CleanEarth", 180, 22, false, true));
        return bots;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
