package com.pinduoduo.trashgo.data.model;

public class Quest {
    private String id;
    private String title;
    private WasteCategory targetCategory;
    private int targetAmount;
    private int currentAmount;
    private int rewardPoints;
    private boolean isObjectiveOfDay;
    private boolean completed;
    private String dateKey;

    public Quest() {}

    public Quest(String id, String title, WasteCategory targetCategory, int targetAmount,
                 int currentAmount, int rewardPoints, boolean isObjectiveOfDay,
                 boolean completed, String dateKey) {
        this.id = id;
        this.title = title;
        this.targetCategory = targetCategory;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.rewardPoints = rewardPoints;
        this.isObjectiveOfDay = isObjectiveOfDay;
        this.completed = completed;
        this.dateKey = dateKey;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public WasteCategory getTargetCategory() { return targetCategory; }
    public void setTargetCategory(WasteCategory targetCategory) { this.targetCategory = targetCategory; }

    public int getTargetAmount() { return targetAmount; }
    public void setTargetAmount(int targetAmount) { this.targetAmount = targetAmount; }

    public int getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(int currentAmount) { this.currentAmount = currentAmount; }

    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }

    public boolean isObjectiveOfDay() { return isObjectiveOfDay; }
    public void setObjectiveOfDay(boolean objectiveOfDay) { isObjectiveOfDay = objectiveOfDay; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }
}
