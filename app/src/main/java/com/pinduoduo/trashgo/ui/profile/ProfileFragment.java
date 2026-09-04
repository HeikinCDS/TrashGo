package com.pinduoduo.trashgo.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.FragmentProfileBinding;
import com.pinduoduo.trashgo.ui.auth.LoginActivity;
import com.pinduoduo.trashgo.ui.settings.SettingsSheet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {
    private static final int DECODE_MAX_SIZE = 1024;
    private static final int SAVED_PHOTO_SIZE = 512;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private FragmentProfileBinding binding;

    @Override
    public void onResume() {
        super.onResume();
        renderHistory();
    }

    private void renderHistory() {
        if (binding == null) return;
        int count = new com.pinduoduo.trashgo.data.repository.ScanHistoryStore(
                requireContext()).read().size();
        binding.historyStatus.setText(count == 0
                ? getString(R.string.history_empty)
                : getResources().getQuantityString(R.plurals.history_count, count, count));
        binding.historyView.setEnabled(count > 0);
        binding.historyView.setOnClickListener(v ->
                ScanHistorySheet.show(getChildFragmentManager()));
    }

    private final ExecutorService photoExecutor = Executors.newSingleThreadExecutor();
    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveProfilePhoto(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        binding.signOutButton.setOnClickListener(view -> signOut());
        binding.profileSettings.setOnClickListener(v ->
                SettingsSheet.show(getParentFragmentManager()));
        binding.profilePhotoContainer.setOnClickListener(v ->
                photoPickerLauncher.launch("image/*"));
        loadProfile();
        return binding.getRoot();
    }

    private void loadProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            signOut();
            return;
        }

        binding.profileName.setText(user.getDisplayName() == null
                ? getString(R.string.trashgo_user) : user.getDisplayName());
        binding.profileEmail.setText(user.getEmail());
        loadProfilePhoto(user.getUid());
        binding.profileProgress.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (binding == null) {
                        return;
                    }
                    binding.profileProgress.setVisibility(View.GONE);
                    if (!task.isSuccessful() || task.getResult() == null) {
                        binding.profileStatus.setText(R.string.profile_load_error);
                        binding.profileStatus.setVisibility(View.VISIBLE);
                        return;
                    }

                    DocumentSnapshot document = task.getResult();
                    String displayName = document.getString("displayName");
                    if (displayName != null && !displayName.isEmpty()) {
                        binding.profileName.setText(displayName);
                    }
                    binding.profilePoints.setText(number(document.getLong("totalPoints")));
                    binding.profileItems.setText(number(document.getLong("itemsRecycled")));
                    binding.profileStreak.setText(number(document.getLong("currentStreak")));
                });
    }

    private void loadProfilePhoto(String uid) {
        File photoFile = profilePhotoFile(requireContext(), uid);
        if (!photoFile.isFile()) {
            showDefaultProfilePhoto();
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        if (bitmap == null) {
            showDefaultProfilePhoto();
        } else {
            showProfilePhoto(bitmap);
        }
    }

    private void saveProfilePhoto(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || binding == null) {
            return;
        }

        Context appContext = requireContext().getApplicationContext();
        String uid = user.getUid();
        binding.profileProgress.setVisibility(View.VISIBLE);

        photoExecutor.execute(() -> {
            try {
                Bitmap bitmap = decodeProfilePhoto(appContext, uri);
                if (bitmap == null) {
                    throw new IOException("Selected image could not be decoded");
                }

                Bitmap resized = resizeBitmap(bitmap, SAVED_PHOTO_SIZE);
                File destination = profilePhotoFile(appContext, uid);
                File parent = destination.getParentFile();
                if (parent == null || (!parent.exists() && !parent.mkdirs())) {
                    throw new IOException("Profile photo directory could not be created");
                }

                try (FileOutputStream output = new FileOutputStream(destination)) {
                    if (!resized.compress(Bitmap.CompressFormat.JPEG, 85, output)) {
                        throw new IOException("Profile photo could not be compressed");
                    }
                }

                mainHandler.post(() -> {
                    if (binding == null) {
                        return;
                    }
                    binding.profileProgress.setVisibility(View.GONE);
                    showProfilePhoto(resized);
                    Toast.makeText(appContext, R.string.profile_photo_updated,
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception error) {
                mainHandler.post(() -> {
                    if (binding == null) {
                        return;
                    }
                    binding.profileProgress.setVisibility(View.GONE);
                    Toast.makeText(appContext, R.string.profile_photo_error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Nullable
    private Bitmap decodeProfilePhoto(Context context, Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(input, null, bounds);
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        while (bounds.outWidth / options.inSampleSize > DECODE_MAX_SIZE
                || bounds.outHeight / options.inSampleSize > DECODE_MAX_SIZE) {
            options.inSampleSize *= 2;
        }

        Bitmap bitmap;
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(input, null, options);
        }
        if (bitmap == null) {
            return null;
        }

        int rotation = photoRotation(context, uri);
        if (rotation == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    private int photoRotation(Context context, Uri uri) {
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) {
                return 0;
            }
            int orientation = new ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            }
        } catch (IOException ignored) {
        }
        return 0;
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        return Bitmap.createScaledBitmap(bitmap, Math.round(width * scale),
                Math.round(height * scale), true);
    }

    private File profilePhotoFile(Context context, String uid) {
        return new File(new File(context.getFilesDir(), "profile_photos"), uid + ".jpg");
    }

    private void showProfilePhoto(Bitmap bitmap) {
        binding.profilePhoto.setImageTintList(null);
        binding.profilePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        binding.profilePhoto.setImageBitmap(bitmap);
    }

    private void showDefaultProfilePhoto() {
        ColorStateList tint = ContextCompat.getColorStateList(
                requireContext(), R.color.trashgo_primary);
        binding.profilePhoto.setImageTintList(tint);
        binding.profilePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        binding.profilePhoto.setImageResource(R.drawable.ic_profile);
    }

    private String number(Long value) {
        return String.valueOf(value == null ? 0 : value);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacksAndMessages(null);
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        photoExecutor.shutdown();
    }
}
