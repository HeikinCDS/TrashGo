package com.pinduoduo.trashgo.data.repository;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.data.verify.VerificationResult;

public interface PointsRepository {

    void claim(@Nullable String qrPayload,
               @NonNull DropOffPoint point,
               @NonNull WasteCategory category,
               @Nullable Location location,
               @NonNull ClaimCallback callback);

    interface ClaimCallback {

        void onAwarded(int pointsAwarded, long newTotal, int newStreak);

        void onRejected(@NonNull VerificationResult reason);

        void onError(@NonNull String message);
    }
}