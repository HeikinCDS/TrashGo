package com.pinduoduo.trashgo.ui.map;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.pinduoduo.trashgo.data.repository.FirestorePointsRepository;
import com.pinduoduo.trashgo.data.repository.PointsRepository;
import com.pinduoduo.trashgo.data.verify.VerificationResult;
import com.pinduoduo.trashgo.databinding.FragmentMapBinding;
import com.pinduoduo.trashgo.util.LocationHelper;
import com.pinduoduo.trashgo.util.Prefs;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

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
    private PointsRepository pointsRepository;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<ScanOptions> qrLauncher;

    /** The point whose QR code the user is currently scanning. */
    @Nullable private DropOffPoint pendingPoint;

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

        qrLauncher = registerForActivityResult(new ScanContract(), this::onQrScanned);
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
        pointsRepository = new FirestorePointsRepository();

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

        v.findViewById(R.id.sheet_scan_qr).setOnClickListener(btn -> {
            sheet.dismiss();
            startClaim(p);
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

    // ------------------------------------------------------------------
    // Claiming points
    // ------------------------------------------------------------------

    private void startClaim(@NonNull DropOffPoint p) {
        if (filterCategory == null) {
            Toast.makeText(requireContext(), R.string.claim_need_scan, Toast.LENGTH_LONG).show();
            return;
        }
        pendingPoint = p;

        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.claim_scan_prompt));
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        qrLauncher.launch(options);
    }

    private void onQrScanned(@Nullable ScanIntentResult result) {
        if (!isAdded() || result == null || result.getContents() == null) {
            pendingPoint = null;
            return;
        }
        final DropOffPoint point = pendingPoint;
        final WasteCategory category = filterCategory;
        pendingPoint = null;

        if (point == null || category == null) {
            return;
        }

        final String payload = result.getContents();

        Toast.makeText(requireContext(), R.string.claim_checking, Toast.LENGTH_SHORT).show();

        locationHelper.currentLocation(location ->
                submitClaim(payload, point, category, location));
    }

    private void submitClaim(@NonNull String payload,
                             @NonNull DropOffPoint point,
                             @NonNull WasteCategory category,
                             @Nullable Location location) {
        if (!isAdded()) {
            return;
        }
        pointsRepository.claim(payload, point, category, location,
                new PointsRepository.ClaimCallback() {
                    @Override
                    public void onAwarded(int pointsAwarded, long newTotal, int newStreak) {
                        if (!isAdded()) return;
                        showClaimResult(true,
                                getString(R.string.claim_success_headline, pointsAwarded),
                                getString(R.string.claim_success_detail,
                                        getString(labelFor(category)), newStreak));
                    }

                    @Override
                    public void onRejected(@NonNull VerificationResult reason) {
                        if (!isAdded()) return;
                        showClaimResult(false,
                                getString(R.string.claim_failed_headline),
                                getString(messageFor(reason)));
                    }

                    @Override
                    public void onError(@NonNull String message) {
                        if (!isAdded()) return;
                        showClaimResult(false,
                                getString(R.string.claim_failed_headline), message);
                    }
                });
    }

    private void showClaimResult(boolean success, @NonNull String headline,
                                 @NonNull String detail) {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());

        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.sheet_claim_result, null);

        ImageView icon = v.findViewById(R.id.claim_icon);
        icon.setImageResource(success ? R.drawable.ic_check : R.drawable.ic_close);
        icon.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(),
                success ? R.color.trashgo_primary : R.color.trashgo_error));

        ((TextView) v.findViewById(R.id.claim_headline)).setText(headline);
        ((TextView) v.findViewById(R.id.claim_detail)).setText(detail);
        v.findViewById(R.id.claim_done).setOnClickListener(b -> sheet.dismiss());

        sheet.setContentView(v);
        sheet.show();

        if (success) {
            requestFix();
        }
    }

    private static int labelFor(@NonNull WasteCategory category) {
        switch (category) {
            case PLASTIC: return R.string.waste_plastic;
            case PAPER:   return R.string.waste_paper;
            case GLASS:   return R.string.waste_glass;
            case METAL:   return R.string.waste_metal;
            case EWASTE:  return R.string.waste_ewaste;
            case ORGANIC: return R.string.waste_organic;
            case GENERAL: return R.string.waste_general;
            default:      return R.string.waste_general;
        }
    }

    private static int messageFor(@NonNull VerificationResult reason) {
        switch (reason) {
            case BAD_PAYLOAD:           return R.string.claim_bad_payload;
            case WRONG_POINT:           return R.string.claim_wrong_point;
            case CATEGORY_NOT_ACCEPTED: return R.string.claim_category_not_accepted;
            case NO_LOCATION:           return R.string.claim_no_location;
            case MOCK_LOCATION:         return R.string.claim_mock_location;
            case TOO_FAR:               return R.string.claim_too_far;
            case COOLDOWN:              return R.string.claim_cooldown;
            default:                    return R.string.claim_failed_headline;
        }
    }

    @Override
    public void onDestroyView() {
        googleMap = null;
        binding = null;
        super.onDestroyView();
    }
}