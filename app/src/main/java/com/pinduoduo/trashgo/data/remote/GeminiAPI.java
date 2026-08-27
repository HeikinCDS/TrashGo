package com.pinduoduo.trashgo.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiAPI {

    @POST("v1/models/gemini-3.6-flash:generateContent")
    Call<GeminiRawResponse> generateContent(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}
