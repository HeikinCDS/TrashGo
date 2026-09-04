package com.pinduoduo.trashgo.ui.scan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.data.repository.DisposalSessionManager;
import com.pinduoduo.trashgo.data.repository.DropOffRepository;
import com.pinduoduo.trashgo.databinding.FragmentDisposalStationSelectionBinding;
import com.pinduoduo.trashgo.ui.map.DropOffAdapter;
import com.pinduoduo.trashgo.ui.map.DropOffDetailSheet;

import java.util.ArrayList;
import java.util.List;

public class DisposalStationSelectionFragment extends Fragment implements DropOffAdapter.OnPointClickListener {
    private FragmentDisposalStationSelectionBinding binding;
    private DisposalSessionManager sessionManager;
    private DropOffAdapter adapter;
    private DropOffRepository repository;
    @Nullable private WasteCategory activeCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDisposalStationSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new DisposalSessionManager(requireContext());

        DisposalSessionManager.PendingScan pendingScan = sessionManager.getPendingScan();
        if (pendingScan == null) {
            Toast.makeText(requireContext(), "No scanned waste item found. Scan an item first!", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        activeCategory = pendingScan.getCategory();
        int pendingPoints = pendingScan.getPoints();

        binding.disposalScannedCategory.setText("Scanned: " + activeCategory.name());
        binding.disposalPendingPoints.setText("+" + pendingPoints + " Pts Pending");

        binding.btnBackDisposalSelection.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        adapter = new DropOffAdapter(this);
        binding.disposalStationsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.disposalStationsRecycler.setAdapter(adapter);

        repository = new DropOffRepository();
        loadStations();
    }

    private void loadStations() {
        repository.fetchAll(new DropOffRepository.PointsCallback() {
            @Override
            public void onLoaded(@NonNull List<DropOffPoint> points) {
                if (binding == null) return;
                List<DropOffPoint> filtered = new ArrayList<>();
                for (DropOffPoint p : points) {
                    if (p.getAcceptedCategories() != null &&
                            (p.getAcceptedCategories().contains(activeCategory) || p.getAcceptedCategories().contains(WasteCategory.GENERAL))) {
                        filtered.add(p);
                    } else {
                        filtered.add(p);
                    }
                }
                adapter.submit(filtered);
            }

            @Override
            public void onError(@NonNull Exception e) {
                if (binding == null) return;
                Toast.makeText(requireContext(), "Failed to load disposal stations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPointClicked(@NonNull DropOffPoint point) {
        DropOffDetailSheet.show(getParentFragmentManager(), point, activeCategory);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
