package com.pinduoduo.trashgo.ui.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.databinding.SheetDropoffDetailBinding;
import com.pinduoduo.trashgo.ui.scan.QrScanFragment;
import com.pinduoduo.trashgo.util.GeoUtils;

import java.util.Locale;

public class DropOffDetailSheet extends BottomSheetDialogFragment {

    public static final String TAG = "DropOffDetailSheet";

    private static final String ARG_POINT_ID = "arg_point_id";
    private static final String ARG_NAME = "arg_name";
    private static final String ARG_LAT = "arg_lat";
    private static final String ARG_LNG = "arg_lng";
    private static final String ARG_HOURS = "arg_hours";
    private static final String ARG_CATEGORIES = "arg_categories";
    private static final String ARG_CATEGORY_FILTER = "arg_category_filter";

    private SheetDropoffDetailBinding binding;

    public static void show(@NonNull FragmentManager fm, @NonNull DropOffPoint point, @Nullable WasteCategory categoryFilter) {
        DropOffDetailSheet sheet = new DropOffDetailSheet();
        Bundle args = new Bundle();
        args.putString(ARG_POINT_ID, point.getId());
        args.putString(ARG_NAME, point.getName());
        args.putDouble(ARG_LAT, point.getLatitude());
        args.putDouble(ARG_LNG, point.getLongitude());
        args.putString(ARG_HOURS, point.getOpeningHours());
        args.putString(ARG_CATEGORIES, point.getAcceptedCategories() != null ? point.getAcceptedCategories().toString() : "All Recyclables");
        if (categoryFilter != null) {
            args.putString(ARG_CATEGORY_FILTER, categoryFilter.name());
        }
        sheet.setArguments(args);
        sheet.show(fm, TAG);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetDropoffDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        String pointId = args.getString(ARG_POINT_ID, "station_1");
        String name = args.getString(ARG_NAME, "Disposal Station");
        double lat = args.getDouble(ARG_LAT, 0);
        double lng = args.getDouble(ARG_LNG, 0);
        String hours = args.getString(ARG_HOURS, "Daily, 8:00 AM - 10:00 PM");
        String categories = args.getString(ARG_CATEGORIES, "Plastic, Paper, Glass, Metal");
        String categoryFilter = args.getString(ARG_CATEGORY_FILTER);

        binding.sheetName.setText(name);
        binding.sheetHours.setText(hours != null && !hours.isEmpty() ? hours : "Daily, 8:00 AM - 10:00 PM");
        binding.sheetAccepted.setText(categories != null && !categories.isEmpty() ? categories : "All Recyclables");
        binding.sheetDistance.setText("Nearby");

        binding.sheetScanQr.setOnClickListener(v -> {
            dismiss();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, QrScanFragment.newInstance(categoryFilter, pointId, name))
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.sheetNavigate.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse(String.format(Locale.US, "google.navigation:q=%f,%f", lat, lng));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (getContext() != null && mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
