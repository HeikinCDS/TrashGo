package com.pinduoduo.trashgo.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pinduoduo.trashgo.BuildConfig;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.SheetSettingsBinding;
import com.pinduoduo.trashgo.util.GeoUtils;
import com.pinduoduo.trashgo.util.Prefs;

public class SettingsSheet extends BottomSheetDialogFragment {

    public static final String TAG = "SettingsSheet";

    private SheetSettingsBinding binding;

    public static void show(@NonNull FragmentManager fm) {
        new SettingsSheet().show(fm, TAG);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.settingsSoundSwitch.setChecked(Prefs.splashSound(requireContext()));
        binding.settingsSoundSwitch.setOnCheckedChangeListener((b, checked) ->
                Prefs.setSplashSound(requireContext(), checked));

        binding.settingsMilesSwitch.setChecked(Prefs.useMiles(requireContext()));
        updateUnitHint();
        binding.settingsMilesSwitch.setOnCheckedChangeListener((b, checked) -> {
            Prefs.setUseMiles(requireContext(), checked);
            updateUnitHint();
        });

        binding.settingsVersion.setText(getString(
                R.string.settings_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }

    private void updateUnitHint() {
        String example = GeoUtils.format(90) + ", " + GeoUtils.format(1400);
        binding.settingsMilesHint.setText(getString(R.string.settings_units_example, example));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}