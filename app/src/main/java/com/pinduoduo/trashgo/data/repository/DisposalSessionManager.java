package com.pinduoduo.trashgo.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pinduoduo.trashgo.data.model.WasteCategory;

public class DisposalSessionManager {

    private static final String PREF_NAME = "trashgo_disposal_session";
    private static final String KEY_HAS_PENDING = "has_pending";
    private static final String KEY_CATEGORY = "pending_category";
    private static final String KEY_POINTS = "pending_points";
    private static final String KEY_TIMESTAMP = "pending_timestamp";

    private final SharedPreferences prefs;

    public DisposalSessionManager(@NonNull Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static class PendingScan {
        private final WasteCategory category;
        private final int points;
        private final long timestamp;

        public PendingScan(WasteCategory category, int points, long timestamp) {
            this.category = category;
            this.points = points;
            this.timestamp = timestamp;
        }

        public WasteCategory getCategory() { return category; }
        public int getPoints() { return points; }
        public long getTimestamp() { return timestamp; }
    }

    public void startPendingScan(@NonNull WasteCategory category, int points) {
        prefs.edit()
                .putBoolean(KEY_HAS_PENDING, true)
                .putString(KEY_CATEGORY, category.name())
                .putInt(KEY_POINTS, points)
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public boolean hasActiveScan() {
        return prefs.getBoolean(KEY_HAS_PENDING, false);
    }

    @Nullable
    public PendingScan getPendingScan() {
        if (!hasActiveScan()) return null;
        String catName = prefs.getString(KEY_CATEGORY, null);
        if (catName == null) return null;
        try {
            WasteCategory cat = WasteCategory.valueOf(catName);
            int pts = prefs.getInt(KEY_POINTS, 40);
            long time = prefs.getLong(KEY_TIMESTAMP, 0);
            return new PendingScan(cat, pts, time);
        } catch (Exception e) {
            return null;
        }
    }

    public void clearPendingScan() {
        prefs.edit().clear().apply();
    }
}
