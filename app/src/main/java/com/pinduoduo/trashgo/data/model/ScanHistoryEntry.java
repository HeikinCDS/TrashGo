package com.pinduoduo.trashgo.data.model;

public class ScanHistoryEntry {
    public String id;
    public String itemName;
    public String category;
    public long scannedAt;
    public String dropOffId;
    public String dropOffName;
    public long droppedOffAt;

    public boolean canComplete(String scanId) {
        return id != null && id.equals(scanId) && droppedOffAt == 0;
    }
}
