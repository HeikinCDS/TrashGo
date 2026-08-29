package com.pinduoduo.trashgo.ui.scan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.remote.GeminiResponse;
import com.pinduoduo.trashgo.data.repository.GeminiRepository;
import com.pinduoduo.trashgo.databinding.FragmentScanBinding;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private GeminiRepository geminiRepository;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startCamera();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Camera permission is required to scan waste.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Failed to start camera.", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(requireContext(), "Camera is not ready.", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(requireContext().getCacheDir(), "trashgo_scan.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri imageUri = Uri.fromFile(photoFile);
                requireActivity().runOnUiThread(() -> processCapturedImage(imageUri));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Capture failed: " + exception.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void processCapturedImage(Uri imageUri) {
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
        if (bitmap == null) {
            Toast.makeText(requireContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loadingOverlay.setVisibility(View.VISIBLE);
        geminiRepository.classifyWaste(bitmap, new GeminiRepository.GeminiCallback() {
            @Override
            public void onSuccess(GeminiResponse response) {
                if (isAdded()) {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    openResultPage(imageUri, response);
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void openResultPage(Uri imageUri, GeminiResponse result) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, ScanResultFragment.newInstance(
                        imageUri,
                        result.getCategory(),
                        result.getConfidence(),
                        result.getTip()
                ))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        binding = null;
    }
}
