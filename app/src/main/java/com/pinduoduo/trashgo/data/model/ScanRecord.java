package com.pinduoduo.trashgo.data.model;

import java.util.Date;

public class ScanRecord {
    private String localId;
    private WasteCategory category;
    private String dropOffId;
    private int pointsAwarded;
    private Date timestamp;
    private boolean synced;

    public ScanRecord() {}

    public ScanRecord(String localId, WasteCategory category, String dropOffId,
                      int pointsAwarded, Date timestamp, boolean synced) {
        this.localId = localId;
        this.category = category;
        this.dropOffId = dropOffId;
        this.pointsAwarded = pointsAwarded;
        this.timestamp = timestamp;
        this.synced = synced;
    }

    public String getLocalId() { return localId; }
    public void setLocalId(String localId) { this.localId = localId; }
    public WasteCategory getCategory() { return category; }
    public void setCategory(WasteCategory category) { this.category = category; }
    public String getDropOffId() { return dropOffId; }
    public void setDropOffId(String dropOffId) { this.dropOffId = dropOffId; }
    public int getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(int pointsAwarded) { this.pointsAwarded = pointsAwarded; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}
