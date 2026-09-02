package com.pinduoduo.trashgo.ui.map;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.data.repository.DropOffRepository;
import com.pinduoduo.trashgo.databinding.FragmentMapBinding;
import com.pinduoduo.trashgo.util.LocationHelper;
import com.pinduoduo.trashgo.util.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_CATEGORY = "arg_category";

    /** UTAR Kampar campus centre — used until we have a GPS fix. */
    private static final LatLng CAMPUS = new LatLng(4.3366214, 101.1421110);
    private static final float CAMPUS_ZOOM = 16f;

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private DropOffRepository repository;
    private LocationHelper locationHelper;
    private DropOffAdapter adapter;
    private ActivityResultLauncher<String[]> permissionLauncher;

    private final List<DropOffPoint> allPoints = new ArrayList<>();
    private boolean pointsLoaded = false;

    @Nullable private WasteCategory filterCategory;
    @Nullable private Double userLat, userLng;

    /** Called by Package B's result screen to open the map filtered to one material. */
    @NonNull
    public static MapFragment newInstance(@Nullable WasteCategory category) {
        MapFragment f = new MapFragment();
        Bundle args = new Bundle();
        if (category != null) args.putString(ARG_CATEGORY, category.name());
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_CATEGORY)) {
            try {
                filterCategory = WasteCategory.valueOf(args.getString(ARG_CATEGORY));
            } catch (Exception e) {
                filterCategory = null;
            }
        }

        // Must be registered before the fragment reaches STARTED, hence onCreate.
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (locationHelper.hasLocationPermission()) {
                        enableMyLocation();
                        requestFix();
                    } else {
                        Toast.makeText(requireContext(),
                                "Location off — showing all campus points",
                                Toast.LENGTH_SHORT).show();
                        renderIfReady();
                    }
                });
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

        Prefs.apply(requireContext());

        repository = new DropOffRepository();
        locationHelper = new LocationHelper(requireContext());

        adapter = new DropOffAdapter(this::showDetail);
        binding.recyclerDropoffs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDropoffs.setAdapter(adapter);

        // getChildFragmentManager, not parent — the map lives inside THIS fragment.
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        loadPoints();

        if (locationHelper.hasLocationPermission()) {
            requestFix();
        } else {
            permissionLauncher.launch(LocationHelper.PERMISSIONS);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(CAMPUS, CAMPUS_ZOOM));

        map.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof DropOffPoint) {
                showDetail((DropOffPoint) tag);
                return true;
            }
            return false;
        });

        enableMyLocation();
        renderIfReady();
    }

    private void enableMyLocation() {
        if (googleMap == null || !locationHelper.hasLocationPermission()) return;
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException ignored) { }
    }

    private void loadPoints() {
        repository.fetchAll(new DropOffRepository.PointsCallback() {
            @Override
            public void onLoaded(@NonNull List<DropOffPoint> points) {
                if (!isAdded()) return;          // fragment gone while network was in flight
                allPoints.clear();
                allPoints.addAll(points);
                pointsLoaded = true;
                renderIfReady();
            }

            @Override
            public void onError(@NonNull Exception e) {
                if (!isAdded()) return;
                pointsLoaded = true;
                Toast.makeText(requireContext(),
                        "Could not load drop-off points", Toast.LENGTH_LONG).show();
                renderIfReady();
            }
        });
    }

    private void requestFix() {
        locationHelper.currentLocation(location -> {
            if (!isAdded() || location == null) return;
            userLat = location.getLatitude();
            userLng = location.getLongitude();
            adapter.setUserLocation(userLat, userLng);
            renderIfReady();
        });
    }

    private void renderIfReady() {
        if (!isAdded() || binding == null || !pointsLoaded) return;

        List<DropOffPoint> visible =
                DropOffRepository.filterByCategory(allPoints, filterCategory);
        visible = DropOffRepository.sortByDistance(visible, userLat, userLng);

        adapter.submit(visible);

        binding.mapHeading.setText(filterCategory == null
                ? String.format(Locale.US, "%d drop-off points", visible.size())
                : String.format(Locale.US, "%d accept %s", visible.size(),
                filterCategory.name().toLowerCase(Locale.US)));

        if (googleMap == null) return;    // markers get drawn when onMapReady fires

        googleMap.clear();
        for (DropOffPoint p : visible) {
            Marker m = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(p.getLatitude(), p.getLongitude()))
                    .title(p.getName())
                    .snippet(DropOffRepository.acceptedLabel(p))
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN)));
            if (m != null) m.setTag(p);    // so the click listener knows which point
        }
    }

    private void showDetail(@NonNull DropOffPoint p) {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());

        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.sheet_dropoff_detail, null);

        ((android.widget.TextView) v.findViewById(R.id.sheet_name)).setText(p.getName());
        ((android.widget.TextView) v.findViewById(R.id.sheet_hours)).setText(p.getOpeningHours());
        ((android.widget.TextView) v.findViewById(R.id.sheet_accepted))
                .setText(DropOffRepository.acceptedLabel(p));

        android.widget.TextView dist = v.findViewById(R.id.sheet_distance);
        if (userLat != null && userLng != null) {
            dist.setText(com.pinduoduo.trashgo.util.GeoUtils.format(
                    com.pinduoduo.trashgo.util.GeoUtils.distanceMetres(
                            userLat, userLng, p.getLatitude(), p.getLongitude())));
        } else {
            dist.setText("—");
        }

        v.findViewById(R.id.sheet_navigate).setOnClickListener(btn -> {
            startNavigation(p);
            sheet.dismiss();
        });

        // also recentre the map behind the sheet
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(p.getLatitude(), p.getLongitude()), 18f));
        }

        sheet.setContentView(v);
        sheet.show();
    }


    private void startNavigation(@NonNull DropOffPoint p) {
        android.net.Uri uri = android.net.Uri.parse(String.format(Locale.US,
                "google.navigation:q=%f,%f&mode=w", p.getLatitude(), p.getLongitude()));
        android.content.Intent intent =
                new android.content.Intent(android.content.Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            android.net.Uri web = android.net.Uri.parse(String.format(Locale.US,
                    "https://www.google.com/maps/dir/?api=1&destination=%f,%f",
                    p.getLatitude(), p.getLongitude()));
            try {
                startActivity(new android.content.Intent(
                        android.content.Intent.ACTION_VIEW, web));
            } catch (android.content.ActivityNotFoundException e2) {
                Toast.makeText(requireContext(),
                        "No maps app installed", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        googleMap = null;
        binding = null;
        super.onDestroyView();
    }
}