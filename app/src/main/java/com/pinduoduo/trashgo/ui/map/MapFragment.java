package com.pinduoduo.trashgo.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.data.repository.DropOffRepository;
import com.pinduoduo.trashgo.databinding.FragmentMapBinding;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements DropOffAdapter.OnPointClickListener {

    private static final String ARG_CATEGORY_FILTER = "arg_category_filter";

    private FragmentMapBinding binding;
    private DropOffAdapter adapter;
    private DropOffRepository repository;
    @Nullable private WasteCategory categoryFilter;

    public static MapFragment newInstance(@Nullable WasteCategory categoryFilter) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        if (categoryFilter != null) {
            args.putString(ARG_CATEGORY_FILTER, categoryFilter.name());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_CATEGORY_FILTER)) {
            String catStr = args.getString(ARG_CATEGORY_FILTER);
            if (catStr != null) {
                try {
                    categoryFilter = WasteCategory.valueOf(catStr);
                } catch (Exception ignored) {}
            }
        }

        if (categoryFilter != null) {
            binding.mapFilterNotice.setText("Step 2: Choose a disposal location for " + categoryFilter.name());
            binding.mapFilterNotice.setVisibility(View.VISIBLE);
        } else {
            binding.mapFilterNotice.setText("Select a disposal station to view details & scan QR code");
            binding.mapFilterNotice.setVisibility(View.VISIBLE);
        }

        adapter = new DropOffAdapter(this);
        binding.mapDropoffList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.mapDropoffList.setAdapter(adapter);

        repository = new DropOffRepository();
        loadStations();
    }

    private void loadStations() {
        repository.fetchAll(new DropOffRepository.PointsCallback() {
            @Override
            public void onLoaded(@NonNull List<DropOffPoint> points) {
                if (binding == null) return;
                List<DropOffPoint> filtered = filterByCategory(points, categoryFilter);
                if (filtered.isEmpty()) {
                    binding.mapStatus.setText("No stations found for this category.");
                    binding.mapStatus.setVisibility(View.VISIBLE);
                } else {
                    binding.mapStatus.setVisibility(View.GONE);
                }
                adapter.submit(filtered);
            }

            @Override
            public void onError(@NonNull Exception e) {
                if (binding == null) return;
                binding.mapStatus.setText(R.string.map_load_failed);
                binding.mapStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    private List<DropOffPoint> filterByCategory(List<DropOffPoint> points, @Nullable WasteCategory filter) {
        if (filter == null || points == null) return points != null ? points : new ArrayList<>();
        List<DropOffPoint> result = new ArrayList<>();
        for (DropOffPoint p : points) {
            if (p.getAcceptedCategories() != null &&
                    (p.getAcceptedCategories().contains(filter) || p.getAcceptedCategories().contains(WasteCategory.GENERAL))) {
                result.add(p);
            } else {
                result.add(p); // Fallback include for campus stations
            }
        }
        return result;
    }

    @Override
    public void onPointClicked(@NonNull DropOffPoint point) {
        DropOffDetailSheet.show(getParentFragmentManager(), point, categoryFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
