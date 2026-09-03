package com.pinduoduo.trashgo.data.remote;

public class GeminiResponse {

    private String category;
    private double confidence;
    private String tip;

    public GeminiResponse() {
    }

    public GeminiResponse(String category, double confidence, String tip) {
        this.category = category;
        this.confidence = confidence;
        this.tip = tip;
    }

    public String getCategory() {
        return category;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getTip() {
        return tip;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }
}
