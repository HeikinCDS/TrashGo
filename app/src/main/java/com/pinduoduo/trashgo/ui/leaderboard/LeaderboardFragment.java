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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pinduoduo.trashgo.data.model.LeaderboardEntry;
import com.pinduoduo.trashgo.databinding.FragmentLeaderboardBinding;

import java.util.ArrayList;
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

        loadRealUserLeaderboard();
    }

    private void loadRealUserLeaderboard() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "";

        FirebaseFirestore.getInstance().collection("users")
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding == null) return;
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    int rank = 1;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String uid = doc.getId();
                        String name = doc.getString("displayName");
                        if (name == null || name.trim().isEmpty()) {
                            name = doc.getString("name");
                        }
                        if (name == null || name.trim().isEmpty()) {
                            name = "TrashGo User";
                        }
                        Long ptsObj = doc.getLong("totalPoints");
                        Long itemsObj = doc.getLong("itemsRecycled");
                        long pts = ptsObj != null ? ptsObj : 0;
                        long items = itemsObj != null ? itemsObj : 0;

                        boolean isSelf = uid.equals(currentUid);
                        LeaderboardEntry entry = new LeaderboardEntry(
                                uid,
                                name,
                                pts,
                                items,
                                isSelf,
                                false
                        );
                        entry.setRank(rank++);
                        entries.add(entry);
                    }

                    if (entries.isEmpty() && currentUser != null) {
                        String selfName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "You";
                        LeaderboardEntry selfEntry = new LeaderboardEntry(
                                currentUid,
                                selfName,
                                0L,
                                0L,
                                true,
                                false
                        );
                        selfEntry.setRank(1);
                        entries.add(selfEntry);
                    }

                    adapter.submitList(entries);
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    Toast.makeText(requireContext(), "Failed to load leaderboard.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
