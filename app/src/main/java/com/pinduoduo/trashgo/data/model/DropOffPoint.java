package com.pinduoduo.trashgo.data.model;

import java.util.List;

public class DropOffPoint {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private List<WasteCategory> acceptedCategories;
    private String openingHours;
    private String qrPayload;

    public DropOffPoint() {}

    public DropOffPoint(String id, String name, double latitude, double longitude,
                        List<WasteCategory> acceptedCategories, String openingHours,
                        String qrPayload) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.acceptedCategories = acceptedCategories;
        this.openingHours = openingHours;
        this.qrPayload = qrPayload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public List<WasteCategory> getAcceptedCategories() { return acceptedCategories; }
    public void setAcceptedCategories(List<WasteCategory> acceptedCategories) { this.acceptedCategories = acceptedCategories; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
}
