package com.pinduoduo.trashgo.ui.profile;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.ScanHistoryEntry;
import com.pinduoduo.trashgo.data.repository.ScanHistoryStore;
import java.text.DateFormat;
import java.util.*;

public final class ScanHistorySheet extends BottomSheetDialogFragment {
    public static void show(FragmentManager manager) {
        if (manager.findFragmentByTag("ScanHistorySheet") == null)
            new ScanHistorySheet().show(manager, "ScanHistorySheet");
    }
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle state) {
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(requireContext());
        title.setText(R.string.history_title);
        title.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_HeadlineSmall);
        content.addView(title);
        List<ScanHistoryEntry> entries = new ScanHistoryStore(requireContext()).read();
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        if (entries.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.history_empty);
            empty.setPadding(0, pad, 0, pad);
            content.addView(empty);
        }
        for (ScanHistoryEntry entry : entries) {
            View row = inflater.inflate(R.layout.item_scan_history, content, false);
            ((TextView) row.findViewById(R.id.history_item)).setText(entry.itemName + " · " + entry.category);
            ((TextView) row.findViewById(R.id.history_scan_time)).setText(
                    getString(R.string.history_scanned, format.format(new Date(entry.scannedAt))));
            TextView location = row.findViewById(R.id.history_location);
            TextView time = row.findViewById(R.id.history_drop_time);
            if (entry.droppedOffAt == 0) {
                location.setText(R.string.history_pending);
                time.setVisibility(View.GONE);
            } else {
                location.setText(getString(R.string.history_station,
                        entry.dropOffName == null ? entry.dropOffId : entry.dropOffName));
                time.setText(getString(R.string.history_dropped,
                        format.format(new Date(entry.droppedOffAt))));
            }
            content.addView(row);
        }
        MaterialButton close = new MaterialButton(requireContext());
        close.setText(R.string.history_close);
        close.setOnClickListener(v -> dismiss());
        content.addView(close);
        ScrollView scroll = new ScrollView(requireContext());
        scroll.addView(content);
        return scroll;
    }
}
