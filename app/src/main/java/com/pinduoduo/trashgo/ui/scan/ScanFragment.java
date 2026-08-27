package com.pinduoduo.trashgo.ui.scan;


import com.pinduoduo.trashgo.util.ImageUtils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.pinduoduo.trashgo.databinding.FragmentScanBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

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
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentScanBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();

        binding.captureButton.setOnClickListener(v -> takePhoto());
        binding.btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        checkCameraPermission();
    }

    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            startCamera();

        } else {

            cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .build();

                preview.setSurfaceProvider(
                        binding.previewView.getSurfaceProvider()
                );

                imageCapture = new ImageCapture.Builder()
                        .build();

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {

                e.printStackTrace();

                Toast.makeText(
                        requireContext(),
                        "Failed to start camera.",
                        Toast.LENGTH_SHORT
                ).show();
            }

        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {

        if (imageCapture == null) {
            Toast.makeText(
                    requireContext(),
                    "Camera is not ready.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        File photoFile = new File(
                requireContext().getCacheDir(),
                "trashgo_scan.jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile)
                        .build();

        imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Uri imageUri = Uri.fromFile(photoFile);

                        requireActivity().runOnUiThread(() -> {

                            Toast.makeText(
                                    requireContext(),
                                    "Photo captured!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            processCapturedImage(imageUri);
                        });
                    }



                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception) {

                        requireActivity().runOnUiThread(() -> Toast.makeText(
                                requireContext(),
                                "Failed to capture photo: "
                                        + exception.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
                    }
                }
        );
    }

    private void processCapturedImage(Uri imageUri) {

        try {

            Bitmap bitmap = BitmapFactory.decodeFile(
                    imageUri.getPath()
            );

            if (bitmap == null) {

                Toast.makeText(
                        requireContext(),
                        "Failed to load captured image.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();

            Bitmap resizedBitmap =
                    ImageUtils.resizeBitmap(bitmap, 1024);

            int resizedWidth = resizedBitmap.getWidth();
            int resizedHeight = resizedBitmap.getHeight();

            String base64 =
                    ImageUtils.processImage(bitmap);

            Toast.makeText(
                    requireContext(),
                    "Image processed successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            System.out.println(
                    "Original: "
                            + originalWidth
                            + " x "
                            + originalHeight
            );

            System.out.println(
                    "Resized: "
                            + resizedWidth
                            + " x "
                            + resizedHeight
            );

            System.out.println(
                    "Base64 length: "
                            + base64.length()
            );

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    requireContext(),
                    "Failed to process image.",
                    Toast.LENGTH_SHORT
            ).show();
        }
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