package com.pinduoduo.trashgo.ui.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.pinduoduo.trashgo.data.model.LeaderboardEntry;
import com.pinduoduo.trashgo.databinding.FragmentLeaderboardBinding;
import com.pinduoduo.trashgo.util.PointTotals;
import java.util.ArrayList;
import java.util.List;

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
    }

    @Override
    public void onStart() {
        super.onStart();
        if (leaderboardListener != null || binding == null) return;
        final FragmentLeaderboardBinding currentBinding = binding;
        // Include older accounts without lifetimePoints. Server-side ordering would omit them.
        // For a larger deployment, backfill all accounts and use orderBy + limit on the server.
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
                    adapter.submitList(entries);
                });
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
