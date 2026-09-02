package com.pinduoduo.trashgo.data.model;

public class LeaderboardEntry {
    private String uid;
    private String displayName;
    private long totalPoints;
    private long itemsRecycled;
    private int rank;
    private boolean isCurrentUser;
    private boolean isBot;
    private String avatarInitial;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String uid, String displayName, long totalPoints, long itemsRecycled,
                            boolean isCurrentUser, boolean isBot) {
        this.uid = uid;
        this.displayName = displayName;
        this.totalPoints = totalPoints;
        this.itemsRecycled = itemsRecycled;
        this.isCurrentUser = isCurrentUser;
        this.isBot = isBot;
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.avatarInitial = displayName.trim().substring(0, 1).toUpperCase();
        } else {
            this.avatarInitial = "U";
        }
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public long getTotalPoints() { return totalPoints; }
    public void setTotalPoints(long totalPoints) { this.totalPoints = totalPoints; }

    public long getItemsRecycled() { return itemsRecycled; }
    public void setItemsRecycled(long itemsRecycled) { this.itemsRecycled = itemsRecycled; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public boolean isCurrentUser() { return isCurrentUser; }
    public void setCurrentUser(boolean currentUser) { isCurrentUser = currentUser; }

    public boolean isBot() { return isBot; }
    public void setBot(boolean bot) { isBot = bot; }

    public String getAvatarInitial() { return avatarInitial; }
    public void setAvatarInitial(String avatarInitial) { this.avatarInitial = avatarInitial; }
}
