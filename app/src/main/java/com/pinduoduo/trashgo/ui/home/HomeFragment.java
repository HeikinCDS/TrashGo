package com.pinduoduo.trashgo.ui.home;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.Quest;
import com.pinduoduo.trashgo.data.repository.DropOffRepository;
import com.pinduoduo.trashgo.data.repository.QuestRepository;
import com.pinduoduo.trashgo.databinding.FragmentHomeBinding;
import com.pinduoduo.trashgo.ui.map.DropOffAdapter;
import com.pinduoduo.trashgo.ui.scan.ScanFragment;
import com.pinduoduo.trashgo.ui.vouchers.VouchersSheet;
import com.pinduoduo.trashgo.util.LocationHelper;
import com.pinduoduo.trashgo.util.Prefs;

import java.util.List;

public class HomeFragment extends Fragment implements DropOffAdapter.OnPointClickListener {

    private static final int PREVIEW_COUNT = 3;

    private FragmentHomeBinding binding;
    private DropOffAdapter adapter;
    private DropOffRepository repository;
    private LocationHelper locationHelper;

    @Nullable private List<DropOffPoint> loadedPoints;
    @Nullable private Double userLat;
    @Nullable private Double userLng;
    private boolean locationSettled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Prefs.apply(requireContext());

        binding.btnScanWaste.setOnClickListener(v -> getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ScanFragment())
                .addToBackStack(null)
                .commit());

        binding.homeSeeAll.setOnClickListener(v -> openMapTab());

        // Open Vouchers & Rewards Sheet
        View.OnClickListener openVouchersListener = v -> VouchersSheet.show(getChildFragmentManager());
        binding.btnOpenVouchers.setOnClickListener(openVouchersListener);
        binding.homeStatRow.setOnClickListener(openVouchersListener);

        // Setup DropOff list adapter
        adapter = new DropOffAdapter(this);
        binding.homeNearestList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.homeNearestList.setAdapter(adapter);

        // Fetch Today's Objective of the Day
        QuestRepository questRepository = new QuestRepository();
        List<Quest> objectiveList = questRepository.getTodayQuests(requireContext());

        // Setup Objective of the Day Adapter
        QuestAdapter objectiveAdapter = new QuestAdapter();
        binding.homeObjectiveList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.homeObjectiveList.setAdapter(objectiveAdapter);
        objectiveAdapter.submitList(objectiveList);
        final FragmentHomeBinding currentBinding = binding;
        questRepository.refresh(requireContext(), new QuestRepository.Callback() {
            @Override
            public void onSuccess(Quest quest, int awarded) {
                if (binding != currentBinding) return;
                objectiveAdapter.submitList(java.util.Collections.singletonList(quest));
            }
            @Override
            public void onError(String message) {
                // Keep this user's local completion state when offline.
            }
        });

        loadUserStats();

        repository = new DropOffRepository();
        locationHelper = new LocationHelper(requireContext());

        loadNearest();
    }

    private void loadUserStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    if (binding == null || snapshot == null) return;
                    Long pts = snapshot.getLong("totalPoints");
                    Long items = snapshot.getLong("itemsRecycled");
                    Long streak = snapshot.getLong("currentStreak");
                    binding.homePoints.setText(String.valueOf(pts != null ? pts : 0));
                    binding.homeItems.setText(String.valueOf(items != null ? items : 0));
                    binding.homeStreak.setText(String.valueOf(streak != null ? streak : 0));
                });
    }

    private void loadNearest() {
        repository.fetchAll(new DropOffRepository.PointsCallback() {
            @Override
            public void onLoaded(@NonNull List<DropOffPoint> points) {
                if (binding == null) return;
                loadedPoints = points;
                renderIfReady();
            }

            @Override
            public void onError(@NonNull Exception e) {
                if (binding == null) return;
                binding.homeNearestStatus.setVisibility(View.VISIBLE);
                binding.homeNearestStatus.setText(R.string.map_load_failed);
            }
        });

        requestLocationIfAllowed();
    }

    private void requestLocationIfAllowed() {
        if (!locationHelper.hasLocationPermission()) {
            locationSettled = true;
            renderIfReady();
            return;
        }

        locationHelper.currentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocation(@Nullable Location location) {
                if (binding == null) return;
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                }
                locationSettled = true;
                renderIfReady();
            }
        });
    }

    private void renderIfReady() {
        if (binding == null || loadedPoints == null || !locationSettled) {
            return;
        }

        if (loadedPoints.isEmpty()) {
            binding.homeNearestStatus.setVisibility(View.VISIBLE);
            binding.homeNearestStatus.setText(R.string.home_nearest_empty);
            binding.homeNearestList.setVisibility(View.GONE);
            return;
        }

        List<DropOffPoint> sorted = DropOffRepository.sortByDistance(loadedPoints, userLat, userLng);
        List<DropOffPoint> preview = sorted.size() > PREVIEW_COUNT
                ? sorted.subList(0, PREVIEW_COUNT)
                : sorted;

        adapter.setUserLocation(userLat, userLng);
        adapter.submit(preview);

        binding.homeNearestStatus.setVisibility(View.GONE);
        binding.homeNearestList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPointClicked(@NonNull DropOffPoint point) {
        openMapTab();
    }

    private void openMapTab() {
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_map);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.homeNearestList.setAdapter(null);
        binding.homeObjectiveList.setAdapter(null);
        binding = null;
        adapter = null;
    }
}
