package com.pinduoduo.trashgo.data.model;

public class Voucher {
    private String id;
    private String title;
    private String description;
    private int pointsCost;
    private String voucherCode;
    private boolean redeemed;

    public Voucher() {}

    public Voucher(String id, String title, String description, int pointsCost, String voucherCode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.pointsCost = pointsCost;
        this.voucherCode = voucherCode;
        this.redeemed = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPointsCost() { return pointsCost; }
    public void setPointsCost(int pointsCost) { this.pointsCost = pointsCost; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public boolean isRedeemed() { return redeemed; }
    public void setRedeemed(boolean redeemed) { this.redeemed = redeemed; }
}
