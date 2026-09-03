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
        List<LeaderboardEntry> entries = getBotAccounts();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (binding == null) return;
                        String name = user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                                ? user.getDisplayName() : "You";
                        long points = 0;
                        long items = 0;

                        if (documentSnapshot.exists()) {
                            Long ptsObj = documentSnapshot.getLong("totalPoints");
                            Long itemsObj = documentSnapshot.getLong("itemsRecycled");
                            if (ptsObj != null) points = ptsObj;
                            if (itemsObj != null) items = itemsObj;
                        }

                        LeaderboardEntry userEntry = new LeaderboardEntry(
                                user.getUid(),
                                name,
                                points,
                                items,
                                true,
                                false
                        );

                        entries.add(userEntry);
                        sortAndDisplay(entries);
                    })
                    .addOnFailureListener(e -> {
                        if (binding == null) return;
                        LeaderboardEntry userEntry = new LeaderboardEntry(
                                user.getUid(),
                                user.getDisplayName() != null ? user.getDisplayName() : "You",
                                240L,
                                8L,
                                true,
                                false
                        );
                        entries.add(userEntry);
                        sortAndDisplay(entries);
                    });
        } else {
            sortAndDisplay(entries);
        }
    }

    private void sortAndDisplay(List<LeaderboardEntry> entries) {
        Collections.sort(entries, (a, b) -> Long.compare(b.getTotalPoints(), a.getTotalPoints()));
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
        adapter.submitList(entries);
    }

    private List<LeaderboardEntry> getBotAccounts() {
        List<LeaderboardEntry> bots = new ArrayList<>();
        bots.add(new LeaderboardEntry("bot_1", "EcoChamp_Alex 🏆", 1420L, 42L, false, true));
        bots.add(new LeaderboardEntry("bot_2", "GreenQueen_Mei 🌿", 980L, 35L, false, true));
        bots.add(new LeaderboardEntry("bot_3", "RecycleKing_Sam 👑", 850L, 29L, false, true));
        bots.add(new LeaderboardEntry("bot_4", "ZeroWaste_Dan ♻️", 640L, 21L, false, true));
        bots.add(new LeaderboardEntry("bot_5", "CleanCampus_Zoe 🌸", 410L, 15L, false, true));
        return bots;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
