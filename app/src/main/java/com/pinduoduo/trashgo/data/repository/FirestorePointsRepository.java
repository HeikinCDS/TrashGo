package com.pinduoduo.trashgo.data.repository;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.pinduoduo.trashgo.data.FirestoreContract;
import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.data.verify.DropOffVerifier;
import com.pinduoduo.trashgo.data.verify.VerificationResult;
import com.pinduoduo.trashgo.util.LocationHelper;
import com.pinduoduo.trashgo.util.PointsTable;
import com.pinduoduo.trashgo.util.StreakCalculator;

import java.util.HashMap;
import java.util.Map;

public class FirestorePointsRepository implements PointsRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirestorePointsRepository() {
        this(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
    }

    public FirestorePointsRepository(@NonNull FirebaseFirestore db, @NonNull FirebaseAuth auth) {
        this.db = db;
        this.auth = auth;
    }

    @Override
    public void claim(@Nullable final String qrPayload,
                      @NonNull final DropOffPoint point,
                      @NonNull final WasteCategory category,
                      @Nullable final Location location,
                      @NonNull final ClaimCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("You are signed out. Sign in and try again.");
            return;
        }

        final String pointId = point.getId();
        if (pointId == null) {
            callback.onRejected(VerificationResult.WRONG_POINT);
            return;
        }

        final DocumentReference userRef =
                db.collection(FirestoreContract.Users.COLLECTION).document(user.getUid());
        final DocumentReference scanRef =
                userRef.collection(FirestoreContract.Scans.COLLECTION).document();

        final Double lat = location == null ? null : location.getLatitude();
        final Double lng = location == null ? null : location.getLongitude();
        final boolean mocked = LocationHelper.isMocked(location);
        final boolean accepts = DropOffRepository.accepts(point, category);
        final long now = System.currentTimeMillis();
        final String today = StreakCalculator.todayString();

        db.runTransaction(new Transaction.Function<Outcome>() {
            @Override
            public Outcome apply(@NonNull Transaction transaction)
                    throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(userRef);

                long lastClaim = readLastClaim(snapshot, pointId);

                VerificationResult result = DropOffVerifier.verify(
                        qrPayload, point, accepts, lat, lng, mocked, lastClaim, now);

                if (!result.isSuccess()) {
                    return Outcome.rejected(result);
                }

                long totalPoints = readLong(snapshot, FirestoreContract.Users.TOTAL_POINTS);
                long itemsRecycled = readLong(snapshot, FirestoreContract.Users.ITEMS_RECYCLED);
                int currentStreak = (int) readLong(snapshot, FirestoreContract.Users.CURRENT_STREAK);
                String lastScanDate =
                        snapshot.getString(FirestoreContract.Users.LAST_SCAN_DATE);

                int awarded = PointsTable.forCategory(category);
                long newTotal = totalPoints + awarded;
                long newItems = itemsRecycled + 1;
                int newStreak = StreakCalculator.next(lastScanDate, today, currentStreak);

                Map<String, Object> claims = new HashMap<>();
                claims.put(pointId, now);

                Map<String, Object> updates = new HashMap<>();
                updates.put(FirestoreContract.Users.TOTAL_POINTS, newTotal);
                updates.put("lifetimePoints", com.pinduoduo.trashgo.util.PointTotals.afterEarning(
                        snapshot.getLong("lifetimePoints"), totalPoints, awarded));
                updates.put(FirestoreContract.Users.ITEMS_RECYCLED, newItems);
                updates.put(FirestoreContract.Users.CURRENT_STREAK, newStreak);
                updates.put(FirestoreContract.Users.LAST_SCAN_DATE, today);
                updates.put(FirestoreContract.Users.LAST_CLAIMS, claims);

                transaction.set(userRef, updates, SetOptions.merge());

                Map<String, Object> scan = new HashMap<>();
                scan.put(FirestoreContract.Scans.CATEGORY, category.name());
                scan.put(FirestoreContract.Scans.DROP_OFF_ID, pointId);
                scan.put(FirestoreContract.Scans.POINTS_AWARDED, awarded);
                scan.put(FirestoreContract.Scans.TIMESTAMP, FieldValue.serverTimestamp());

                transaction.set(scanRef, scan);

                return Outcome.awarded(awarded, newTotal, newStreak);
            }
        }).addOnSuccessListener(outcome -> {
            if (outcome.result.isSuccess()) {
                callback.onAwarded(outcome.pointsAwarded, outcome.newTotal, outcome.newStreak);
            } else {
                callback.onRejected(outcome.result);
            }
        }).addOnFailureListener(e -> {
            String message = e.getMessage();
            callback.onError(message == null ? "Could not reach the server." : message);
        });
    }

    private static long readLastClaim(@NonNull DocumentSnapshot snapshot,
                                      @NonNull String pointId) {
        if (!snapshot.exists()) {
            return 0L;
        }
        Object raw = snapshot.get(
                FieldPath.of(FirestoreContract.Users.LAST_CLAIMS, pointId));
        return raw instanceof Number ? ((Number) raw).longValue() : 0L;
    }

    private static long readLong(@NonNull DocumentSnapshot snapshot, @NonNull String field) {
        if (!snapshot.exists()) {
            return 0L;
        }
        Long value = snapshot.getLong(field);
        return value == null ? 0L : value;
    }

    private static final class Outcome {
        final VerificationResult result;
        final int pointsAwarded;
        final long newTotal;
        final int newStreak;

        private Outcome(VerificationResult result, int pointsAwarded,
                        long newTotal, int newStreak) {
            this.result = result;
            this.pointsAwarded = pointsAwarded;
            this.newTotal = newTotal;
            this.newStreak = newStreak;
        }

        static Outcome awarded(int points, long total, int streak) {
            return new Outcome(VerificationResult.OK, points, total, streak);
        }

        static Outcome rejected(VerificationResult result) {
            return new Outcome(result, 0, 0L, 0);
        }
    }
}
