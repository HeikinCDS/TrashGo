package com.pinduoduo.trashgo.ui.scan;

import com.pinduoduo.trashgo.data.remote.GeminiResponse;
import com.pinduoduo.trashgo.data.repository.GeminiRepository;
import com.pinduoduo.trashgo.util.ImageUtils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.camera.core.ImageCaptureException;
import java.io.File;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.FragmentScanBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {
    private static final int MAX_SCAN_PX = 1600;

    private FragmentScanBinding binding;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private GeminiRepository geminiRepository;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startCamera();
                        } else {
                            toast("Camera permission is required to scan waste.");
                        }
                    }
            );

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        geminiRepository = new GeminiRepository();

        binding.captureButton.setOnClickListener(v -> takePhoto());
        binding.btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        checkCameraPermission();
    }

    private void toast(@NonNull String message) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            if (binding == null) {
                return;
            }
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                e.printStackTrace();
                toast("Failed to start camera.");
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            toast("Camera is not ready.");
            return;
        }

        File photoFile = new File(requireContext().getCacheDir(), "trashgo_scan.jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri imageUri = Uri.fromFile(photoFile);
                        mainHandler.post(() -> processCapturedImage(imageUri));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        mainHandler.post(() ->
                                toast("Failed to capture photo: " + exception.getMessage()));
                    }
                }
        );
    }

    private void processCapturedImage(Uri imageUri) {
        if (binding == null) {
            return;
        }

        String path = imageUri.getPath();
        Bitmap bitmap = decodeSampled(path, MAX_SCAN_PX);

        if (bitmap == null) {
            toast("Failed to load captured image.");
            return;
        }

        sendToAi(bitmap, path);
    }

    @Nullable
    private static Bitmap decodeSampled(@Nullable String path, int maxPx) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bounds);

            int longest = Math.max(bounds.outWidth, bounds.outHeight);
            int sample = 1;
            while (longest / sample > maxPx) {
                sample *= 2;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendToAi(Bitmap bitmap, String photoPath) {
        if (binding == null) {
            return;
        }
        binding.loadingOverlay.setVisibility(View.VISIBLE);

        geminiRepository.classifyWaste(bitmap, new GeminiRepository.GeminiCallback() {
            @Override
            public void onSuccess(GeminiResponse response) {
                if (binding == null) {
                    return;
                }
                binding.loadingOverlay.setVisibility(View.GONE);
                showResult(response, photoPath);
            }

            @Override
            public void onError(String message) {
                if (binding == null) {
                    return;
                }
                binding.loadingOverlay.setVisibility(View.GONE);
                toast(message);
            }
        });
    }

    private void showResult(GeminiResponse response, String photoPath) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        new com.pinduoduo.trashgo.data.repository.ScanHistoryStore(context)
                .record(response.getItemName(), response.getCategory());
        try {
            com.pinduoduo.trashgo.data.model.WasteCategory scannedCategory =
                    com.pinduoduo.trashgo.data.model.WasteCategory.valueOf(
                            response.getCategory().toUpperCase(java.util.Locale.US));
            new com.pinduoduo.trashgo.data.repository.DisposalSessionManager(context)
                    .startPendingScan(scannedCategory,
                            com.pinduoduo.trashgo.data.repository.PointsRepositoryImpl.getCategoryPoints(scannedCategory));
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }

        FragmentManager fm = getParentFragmentManager();
        if (fm.isDestroyed()) {
            return;
        }

        FragmentTransaction tx = fm.beginTransaction()
                .replace(R.id.fragment_container, ResultFragment.newInstance(
                        response.getCategory(),
                        response.getConfidence(),
                        response.getTip(),
                        photoPath))
                .addToBackStack(null);

        if (fm.isStateSaved()) {
            tx.commitAllowingStateLoss();
        } else {
            tx.commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mainHandler.removeCallbacksAndMessages(null);

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }

        binding = null;
    }
}
