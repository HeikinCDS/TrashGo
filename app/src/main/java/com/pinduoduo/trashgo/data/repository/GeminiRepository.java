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
    private final String apiKey = BuildConfig.GEMINI_API_KEY;
    private final Gson gson = new Gson();

    public GeminiRepository() {
        this.apiService = RetrofitClient.getGeminiApi();
    }

    public interface GeminiCallback {
        void onSuccess(GeminiResponse response);
        void onError(String message);
    }

    public void classifyWaste(Bitmap bitmap, GeminiCallback callback) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("Gemini API key is not configured.");
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

                        // Handle cases where Gemini might wrap the JSON in Markdown code blocks
                        String cleanJson = textResponse.trim();
                        if (cleanJson.startsWith("```")) {
                            cleanJson = cleanJson.substring(cleanJson.indexOf("{"), cleanJson.lastIndexOf("}") + 1);
                        }
                        
                        GeminiResponse result = gson.fromJson(cleanJson, GeminiResponse.class);
                        callback.onSuccess(result);
                    } catch (Exception e) {
                        Log.e("GeminiAI", "Parse error: " + e.getMessage());
                        callback.onError("Failed to parse AI response: " + e.getMessage());
                    }
                } else {
                    String errorMsg = "API Error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("GeminiAI", "API Error Body: " + errorBody);
                            errorMsg += " - " + errorBody;
                        }
                    } catch (Exception ignored) {}
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<GeminiRawResponse> call, Throwable t) {
                Log.e("GeminiAI", "Network error: " + t.getMessage());
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }
}
