package com.pinduoduo.trashgo.data.model;

public class User {
    private String uid;
    private String displayName;
    private String email;
    private long totalPoints;
    private long itemsRecycled;
    private int currentStreak;
    private String lastScanDate;

    public User() {}

    public User(String uid, String displayName, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public long getTotalPoints() { return totalPoints; }
    public void setTotalPoints(long totalPoints) { this.totalPoints = totalPoints; }
    public long getItemsRecycled() { return itemsRecycled; }
    public void setItemsRecycled(long itemsRecycled) { this.itemsRecycled = itemsRecycled; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public String getLastScanDate() { return lastScanDate; }
    public void setLastScanDate(String lastScanDate) { this.lastScanDate = lastScanDate; }
}
