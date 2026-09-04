package com.pinduoduo.trashgo.data.model;

public class MyVoucher {
    private String id;
    private String title;
    private String code;
    private String claimDate;
    private int pointsSpent;

    public MyVoucher() {}

    public MyVoucher(String id, String title, String code, String claimDate, int pointsSpent) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.claimDate = claimDate;
        this.pointsSpent = pointsSpent;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getClaimDate() { return claimDate; }
    public void setClaimDate(String claimDate) { this.claimDate = claimDate; }

    public int getPointsSpent() { return pointsSpent; }
    public void setPointsSpent(int pointsSpent) { this.pointsSpent = pointsSpent; }
}
