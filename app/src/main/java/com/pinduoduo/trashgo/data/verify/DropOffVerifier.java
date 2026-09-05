package com.pinduoduo.trashgo.data.verify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.util.GeoUtils;

import java.util.Locale;

public final class DropOffVerifier {
    public static final String PAYLOAD_PREFIX = "TRASHGO:DROP_OFF:";
    public static final double MAX_DISTANCE_METRES = 200d;
    public static final long COOLDOWN_MILLIS = 30L * 60L * 1000L;

    public static boolean enforceDistance = false;
    public static boolean enforceCooldown = false;

    private DropOffVerifier() {}

    @Nullable
    public static String parseId(@Nullable String payload) {
        if (payload == null) {
            return null;
        }
        String trimmed = payload.trim();
        if (trimmed.length() <= PAYLOAD_PREFIX.length()) {
            return null;
        }
        if (!trimmed.toUpperCase(Locale.US).startsWith(PAYLOAD_PREFIX)) {
            return null;
        }
        String id = trimmed.substring(PAYLOAD_PREFIX.length()).trim();
        return id.isEmpty() ? null : id;
    }

    @NonNull
    public static VerificationResult verify(
            @Nullable String payload,
            @Nullable DropOffPoint point,
            boolean pointAcceptsCategory,
            @Nullable Double userLat,
            @Nullable Double userLng,
            boolean mockLocation,
            long lastClaimMillis,
            long nowMillis) {
        String scannedId = parseId(payload);
        if (scannedId == null) {
            return VerificationResult.BAD_PAYLOAD;
        }
        if (point == null || point.getId() == null) {
            return VerificationResult.WRONG_POINT;
        }
        if (!scannedId.equals(point.getId())) {
            return VerificationResult.WRONG_POINT;
        }
        if (!pointAcceptsCategory) {
            return VerificationResult.CATEGORY_NOT_ACCEPTED;
        }
        if (mockLocation) {
            return VerificationResult.MOCK_LOCATION;
        }
        if (userLat == null || userLng == null) {
            return VerificationResult.NO_LOCATION;
        }

        if (enforceDistance) {
            double distance = GeoUtils.distanceMetres(
                    userLat, userLng, point.getLatitude(), point.getLongitude());
            if (distance > MAX_DISTANCE_METRES) {
                return VerificationResult.TOO_FAR;
            }
        }

        if (enforceCooldown
                && lastClaimMillis > 0L
                && nowMillis - lastClaimMillis < COOLDOWN_MILLIS) {
            return VerificationResult.COOLDOWN;
        }

        return VerificationResult.OK;
    }
}
