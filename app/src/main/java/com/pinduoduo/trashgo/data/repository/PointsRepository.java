package com.pinduoduo.trashgo.data.repository;

import android.location.Location;

import androidx.annotation.NonNull;

import com.pinduoduo.trashgo.data.model.WasteCategory;

public interface PointsRepository {
    void awardPoints(
            @NonNull String dropOffId,
            @NonNull WasteCategory category,
            @NonNull Location location,
            @NonNull AwardCallback callback
    );

    interface AwardCallback {
        void onSuccess(int pointsAwarded);
        void onError(@NonNull String message);
    }
}
