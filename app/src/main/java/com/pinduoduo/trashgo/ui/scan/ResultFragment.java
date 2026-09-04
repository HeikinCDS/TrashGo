package com.pinduoduo.trashgo.ui.scan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.databinding.FragmentResultBinding;
import com.pinduoduo.trashgo.ui.map.MapFragment;

import java.io.File;
import java.util.Locale;

public class ResultFragment extends Fragment {

    private static final String ARG_CATEGORY = "arg_category";
    private static final String ARG_CONFIDENCE = "arg_confidence";
    private static final String ARG_TIP = "arg_tip";
    private static final String ARG_PHOTO = "arg_photo";

    private static final double LOW_CONFIDENCE = 0.60d;
    private static final int MAX_PHOTO_PX = 1080;

    private FragmentResultBinding binding;

    @Nullable private WasteCategory category;

    public static ResultFragment newInstance(@Nullable String category,
                                             double confidence,
                                             @Nullable String tip,
                                             @Nullable String photoPath) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        args.putDouble(ARG_CONFIDENCE, confidence);
        args.putString(ARG_TIP, tip);
        args.putString(ARG_PHOTO, photoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String raw = args == null ? null : args.getString(ARG_CATEGORY);
        double confidence = args == null ? 0d : args.getDouble(ARG_CONFIDENCE, 0d);
        String tip = args == null ? null : args.getString(ARG_TIP);
        String photoPath = args == null ? null : args.getString(ARG_PHOTO);

        category = parseCategory(raw);

        showPhoto(photoPath);

        binding.resultCategory.setText(labelFor(category));

        int percent = (int) Math.round(clamp(confidence) * 100d);
        binding.resultConfidenceBar.setProgress(percent);
        binding.resultConfidence.setText(getString(R.string.result_confidence, percent));
        binding.resultWarning.setVisibility(
                confidence < LOW_CONFIDENCE ? View.VISIBLE : View.GONE);

        binding.resultTip.setText(tip == null || tip.trim().isEmpty()
                ? getString(R.string.result_no_tip) : tip);

        binding.resultFind.setOnClickListener(v -> openMapForCategory());
        binding.resultAgain.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.resultClose.setOnClickListener(v -> closeToHome());
    }

    private void showPhoto(@Nullable String path) {
        Bitmap photo = loadPhoto(path);
        if (photo == null) {
            binding.resultPhotoCard.setVisibility(View.GONE);
            return;
        }
        binding.resultPhotoCard.setVisibility(View.VISIBLE);
        binding.resultPhoto.setImageBitmap(photo);
    }

    @Nullable
    private static Bitmap loadPhoto(@Nullable String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bounds);

            int longest = Math.max(bounds.outWidth, bounds.outHeight);
            int sample = 1;
            while (longest / sample > MAX_PHOTO_PX) {
                sample *= 2;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap == null) {
                return null;
            }
            return applyExifRotation(bitmap, path);
        } catch (Exception e) {
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private static Bitmap applyExifRotation(@NonNull Bitmap bitmap, @NonNull String path) {
        try {
            int orientation = new ExifInterface(path).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int degrees;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                degrees = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                degrees = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                degrees = 270;
            } else {
                return bitmap;
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            Bitmap rotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) {
                bitmap.recycle();
            }
            return rotated;
        } catch (Exception e) {
            return bitmap;
        } catch (OutOfMemoryError e) {
            return bitmap;
        }
    }

    private static double clamp(double value) {
        if (value < 0d) return 0d;
        if (value > 1d) return 1d;
        return value;
    }

    @Nullable
    private static WasteCategory parseCategory(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.toUpperCase(Locale.US).replaceAll("[^A-Z]", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.equals("EWASTE") || cleaned.equals("ELECTRONIC")
                || cleaned.equals("ELECTRONICS")) {
            return WasteCategory.EWASTE;
        }
        if (cleaned.equals("FOOD") || cleaned.equals("COMPOST")) {
            return WasteCategory.ORGANIC;
        }
        try {
            return WasteCategory.valueOf(cleaned);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static int labelFor(@Nullable WasteCategory category) {
        if (category == null) {
            return R.string.result_unknown;
        }
        switch (category) {
            case PLASTIC: return R.string.waste_plastic;
            case PAPER:   return R.string.waste_paper;
            case GLASS:   return R.string.waste_glass;
            case METAL:   return R.string.waste_metal;
            case EWASTE:  return R.string.waste_ewaste;
            case ORGANIC: return R.string.waste_organic;
            case GENERAL: return R.string.waste_general;
            default:      return R.string.result_unknown;
        }
    }

    private void openMapForCategory() {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_map);
        }

        fm.beginTransaction()
                .replace(R.id.fragment_container, MapFragment.newInstance(category))
                .commit();
    }

    private void closeToHome() {
        requireActivity().getSupportFragmentManager()
                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.resultPhoto.setImageDrawable(null);
        binding = null;
    }
}