package com.pinduoduo.trashgo.ui.scan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.Quest;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.data.repository.PointsRepositoryImpl;
import com.pinduoduo.trashgo.databinding.FragmentQrScanBinding;

import java.util.List;

public class QrScanFragment extends Fragment {

    private static final String ARG_CATEGORY = "arg_category";
    private static final String ARG_STATION_ID = "arg_station_id";
    private static final String ARG_STATION_NAME = "arg_station_name";

    private FragmentQrScanBinding binding;
    private WasteCategory category = WasteCategory.GENERAL;
    private String stationName = "Disposal Station";
    private String stationId = "station_1";

    public static QrScanFragment newInstance(@Nullable String categoryName, @Nullable String stationId, @Nullable String stationName) {
        QrScanFragment fragment = new QrScanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, categoryName);
        args.putString(ARG_STATION_ID, stationId);
        args.putString(ARG_STATION_NAME, stationName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQrScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String catStr = args.getString(ARG_CATEGORY);
            if (catStr != null) {
                try {
                    category = WasteCategory.valueOf(catStr);
                } catch (Exception ignored) {}
            }
            if (args.getString(ARG_STATION_NAME) != null) {
                stationName = args.getString(ARG_STATION_NAME);
            }
            if (args.getString(ARG_STATION_ID) != null) {
                stationId = args.getString(ARG_STATION_ID);
            }
        }

        binding.qrTitle.setText("Scan QR at " + stationName);
        binding.btnQrClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnConfirmQrScan.setOnClickListener(v -> processQrDisposalConfirmation());

        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.qrPreviewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void processQrDisposalConfirmation() {
        binding.qrLoadingOverlay.setVisibility(View.VISIBLE);

        new PointsRepositoryImpl().awardScanPoints(
                requireContext(),
                category,
                new PointsRepositoryImpl.AwardScanCallback() {
                    @Override
                    public void onSuccess(int basePoints, int questBonusPoints, int totalEarnedPoints, @NonNull List<Quest> completedQuests) {
                        if (binding == null) return;
                        binding.qrLoadingOverlay.setVisibility(View.GONE);

                        // Clear pending disposal session
                        new com.pinduoduo.trashgo.data.repository.DisposalSessionManager(requireContext()).clearPendingScan();

                        String questNotice = null;
                        if (!completedQuests.isEmpty()) {
                            StringBuilder sb = new StringBuilder("🎯 Quest Completed! ");
                            for (Quest q : completedQuests) {
                                sb.append(q.getTitle()).append(" (+").append(q.getRewardPoints()).append(" pts) ");
                            }
                            questNotice = sb.toString().trim();
                        }

                        DisposalSuccessDialog.show(getParentFragmentManager(), totalEarnedPoints, stationName, questNotice);
                    }

                    @Override
                    public void onError(@NonNull String error) {
                        if (binding == null) return;
                        binding.qrLoadingOverlay.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Disposal Error: " + error, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
