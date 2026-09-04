package com.pinduoduo.trashgo.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pinduoduo.trashgo.data.model.ScanHistoryEntry;
import java.util.*;

public final class ScanHistoryStore {
    private static final Object LOCK = new Object();
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    public ScanHistoryStore(Context context) {
        String uid = FirebaseAuth.getInstance().getUid();
        prefs = context.getApplicationContext().getSharedPreferences(
                "scan_history_" + (uid == null ? "signed_out" : uid), Context.MODE_PRIVATE);
    }
    public List<ScanHistoryEntry> read() {
        synchronized (LOCK) {
            List<ScanHistoryEntry> entries = gson.fromJson(prefs.getString("entries", "[]"),
                    new TypeToken<List<ScanHistoryEntry>>() {}.getType());
            return entries == null ? new ArrayList<>() : entries;
        }
    }
    private void save(List<ScanHistoryEntry> entries) {
        prefs.edit().putString("entries", gson.toJson(entries)).apply();
    }
    public void record(String name, String category) {
        synchronized (LOCK) {
            ScanHistoryEntry entry = new ScanHistoryEntry();
            entry.id = UUID.randomUUID().toString();
            entry.category = category == null ? "Unknown" : category;
            entry.itemName = name == null || name.trim().isEmpty() ? entry.category + " item" : name.trim();
            entry.scannedAt = System.currentTimeMillis();
            List<ScanHistoryEntry> entries = read();
            entries.add(0, entry);
            save(entries);
            prefs.edit().putString("pending_id", entry.id).apply();
        }
    }
    public String pendingId(String category) {
        synchronized (LOCK) {
            String id = prefs.getString("pending_id", null);
            for (ScanHistoryEntry entry : read()) {
                if (entry.canComplete(id) && entry.category.equalsIgnoreCase(category)) return id;
            }
            return null;
        }
    }
    public void complete(String id, String stationId, String stationName) {
        synchronized (LOCK) {
            if (id == null) return;
            List<ScanHistoryEntry> entries = read();
            for (ScanHistoryEntry entry : entries) {
                if (entry.canComplete(id)) {
                    entry.dropOffId = stationId;
                    entry.dropOffName = stationName;
                    entry.droppedOffAt = System.currentTimeMillis();
                    save(entries);
                    if (id.equals(prefs.getString("pending_id", null)))
                        prefs.edit().remove("pending_id").apply();
                    return;
                }
            }
        }
    }
}
