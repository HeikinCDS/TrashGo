package com.pinduoduo.trashgo.ui.profile;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.ScanHistoryEntry;
import com.pinduoduo.trashgo.data.repository.ScanHistoryStore;
import com.pinduoduo.trashgo.databinding.ItemScanHistoryBinding;
import com.pinduoduo.trashgo.databinding.SheetScanHistoryBinding;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ScanHistorySheet extends BottomSheetDialogFragment {

    public static final String TAG = "ScanHistorySheet";

    private SheetScanHistoryBinding binding;

    public static void show(@NonNull FragmentManager fm) {
        if (fm.isDestroyed() || fm.isStateSaved()) {
            return;
        }
        new ScanHistorySheet().show(fm, TAG);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetScanHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            BottomSheetBehavior<?> behavior = ((BottomSheetDialog) dialog).getBehavior();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.sheetHistoryClose.setOnClickListener(v -> dismiss());

        List<ScanHistoryEntry> entries = new ScanHistoryStore(requireContext()).read();

        binding.sheetHistoryStatus.setText(entries.isEmpty()
                ? getString(R.string.history_empty)
                : getResources().getQuantityString(
                        R.plurals.history_count, entries.size(), entries.size()));

        DateFormat dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT);

        binding.sheetHistoryEntries.removeAllViews();
        for (ScanHistoryEntry entry : entries) {
            ItemScanHistoryBinding row = ItemScanHistoryBinding.inflate(
                    getLayoutInflater(), binding.sheetHistoryEntries, false);

            row.historyItem.setText(entry.itemName + " · " + entry.category);
            row.historyScanTime.setText(getString(R.string.history_scanned,
                    dateFormat.format(new Date(entry.scannedAt))));
            row.historyLocation.setText(entry.droppedOffAt == 0
                    ? getString(R.string.history_pending)
                    : getString(R.string.history_station, entry.dropOffName == null
                            ? entry.dropOffId : entry.dropOffName));
            row.historyDropTime.setVisibility(entry.droppedOffAt == 0 ? View.GONE : View.VISIBLE);
            if (entry.droppedOffAt != 0) {
                row.historyDropTime.setText(getString(R.string.history_dropped,
                        dateFormat.format(new Date(entry.droppedOffAt))));
            }

            binding.sheetHistoryEntries.addView(row.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
