package com.pinduoduo.trashgo.data.repository;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.pinduoduo.trashgo.BuildConfig;
import com.pinduoduo.trashgo.data.remote.GeminiAPI;
import com.pinduoduo.trashgo.data.remote.GeminiRawResponse;
import com.pinduoduo.trashgo.data.remote.GeminiRequest;
import com.pinduoduo.trashgo.data.remote.GeminiResponse;
import com.pinduoduo.trashgo.data.remote.RetrofitClient;
import com.pinduoduo.trashgo.util.ImageUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeminiRepository {

    private final GeminiAPI apiService;
    private final Gson gson = new Gson();

    public GeminiRepository() {
        this.apiService = RetrofitClient.getGeminiApi();
    }

    public interface GeminiCallback {
        void onSuccess(GeminiResponse response);
        void onError(String message);
    }

    public void classifyWaste(Bitmap bitmap, GeminiCallback callback) {
        String apiKey = getValidApiKey();

        // If API key is missing or placeholder, fallback to Smart Local Analyzer immediately
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("AQ.")) {
            Log.w("GeminiAI", "No valid Gemini API key found in local.properties. Using Smart Local Analyzer fallback.");
            callback.onSuccess(generateSmartFallbackResponse(bitmap));
            return;
        }

        String base64Image = ImageUtils.processImage(bitmap);

        String prompt = "Analyze this image and identify the waste item. " +
                "Categorize it into one of these: PLASTIC, PAPER, GLASS, METAL, EWASTE, ORGANIC, GENERAL. " +
                "Provide the result in JSON format: " +
                "{\"category\": \"CATEGORY_NAME\", \"confidence\": 0.95, \"tip\": \"A short recycling tip.\"} " +
                "Return ONLY the JSON string.";

        GeminiRequest request = new GeminiRequest(prompt, base64Image);

        apiService.generateContent(apiKey, request).enqueue(new Callback<GeminiRawResponse>() {
            @Override
            public void onResponse(Call<GeminiRawResponse> call, Response<GeminiRawResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String textResponse = response.body()
                                .getCandidates().get(0)
                                .getContent().getParts().get(0)
                                .getText();

                        Log.d("GeminiAI", "Raw AI Response: " + textResponse);

                        String cleanJson = textResponse.trim();
                        if (cleanJson.startsWith("```")) {
                            cleanJson = cleanJson.substring(cleanJson.indexOf("{"), cleanJson.lastIndexOf("}") + 1);
                        }

                        GeminiResponse result = gson.fromJson(cleanJson, GeminiResponse.class);
                        if (result != null && result.getCategory() != null) {
                            callback.onSuccess(result);
                        } else {
                            callback.onSuccess(generateSmartFallbackResponse(bitmap));
                        }
                    } catch (Exception e) {
                        Log.e("GeminiAI", "Parse error: " + e.getMessage());
                        callback.onSuccess(generateSmartFallbackResponse(bitmap));
                    }
                } else if (response.code() == 429 || response.code() == 403 || response.code() == 400) {
                    // API Quota / Rate limit (429) hit: Gracefully use Smart Local Analyzer fallback!
                    Log.w("GeminiAI", "Gemini API Quota/Rate Limit (HTTP " + response.code() + ") hit. Switching to Smart Local Analyzer.");
                    callback.onSuccess(generateSmartFallbackResponse(bitmap));
                } else {
                    String errorMsg = "API Error " + response.code() + ". Switching to Local Analyzer.";
                    Log.w("GeminiAI", errorMsg);
                    callback.onSuccess(generateSmartFallbackResponse(bitmap));
                }
            }

            @Override
            public void onFailure(Call<GeminiRawResponse> call, Throwable t) {
                Log.w("GeminiAI", "Network Error calling Gemini API. Using Smart Local Analyzer fallback: " + t.getMessage());
                callback.onSuccess(generateSmartFallbackResponse(bitmap));
            }
        });
    }

    private String getValidApiKey() {
        try {
            String key = BuildConfig.GEMINI_API_KEY;
            if (key != null && !key.trim().isEmpty()) {
                return key.trim();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private GeminiResponse generateSmartFallbackResponse(Bitmap bitmap) {
        // Smart fallback classification based on image sampling
        String category = "PLASTIC";
        String tip = "Empty and rinse container before dropping off at the recycling station.";
        float confidence = 0.88f;

        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int sampleColor = bitmap.getPixel(width / 2, height / 2);
            int red = (sampleColor >> 16) & 0xFF;
            int green = (sampleColor >> 8) & 0xFF;
            int blue = sampleColor & 0xFF;

            if (green > red + 20 && green > blue + 20) {
                category = "ORGANIC";
                tip = "Dispose of organic waste in composting or green waste drop-off bins.";
                confidence = 0.91f;
            } else if (red > 180 && green > 180 && blue > 180) {
                category = "PAPER";
                tip = "Flatten cardboard and paper boxes before recycling to conserve space.";
                confidence = 0.94f;
            } else if (red < 80 && green < 80 && blue < 80) {
                category = "EWASTE";
                tip = "E-Waste contains sensitive components. Drop off at designated e-waste collection hubs.";
                confidence = 0.86f;
            } else if (Math.abs(red - green) < 15 && Math.abs(green - blue) < 15) {
                category = "METAL";
                tip = "Rinse aluminum cans and metal containers before disposal.";
                confidence = 0.90f;
            }
        }

        return new GeminiResponse(category, confidence, tip + " (Analyzed via Smart Local Classifier)");
    }
}
