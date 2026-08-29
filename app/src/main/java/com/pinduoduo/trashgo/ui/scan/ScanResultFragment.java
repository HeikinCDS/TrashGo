package com.pinduoduo.trashgo.ui.scan;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pinduoduo.trashgo.databinding.FragmentScanResultBinding;

import java.util.Locale;

public class ScanResultFragment extends Fragment {

    private FragmentScanResultBinding binding;

    private static final String ARG_IMAGE_URI = "image_uri";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_CONFIDENCE = "confidence";
    private static final String ARG_TIP = "tip";

    public static ScanResultFragment newInstance(Uri imageUri, String category, double confidence, String tip) {
        ScanResultFragment fragment = new ScanResultFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, imageUri);
        args.putString(ARG_CATEGORY, category);
        args.putDouble(ARG_CONFIDENCE, confidence);
        args.putString(ARG_TIP, tip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScanResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Uri imageUri = getArguments().getParcelable(ARG_IMAGE_URI);
            String category = getArguments().getString(ARG_CATEGORY);
            double confidence = getArguments().getDouble(ARG_CONFIDENCE);
            String tip = getArguments().getString(ARG_TIP);

            binding.imageResult.setImageURI(imageUri);
            binding.textCategory.setText("Category: " + category);
            binding.textConfidence.setText(String.format(Locale.getDefault(), "Confidence: %.0f%%", confidence * 100));
            binding.textTip.setText("Tip: " + tip);
        }

        binding.buttonScanAgain.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
