package com.pinduoduo.trashgo.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinduoduo.trashgo.data.model.Quest;
import com.pinduoduo.trashgo.data.model.WasteCategory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PointsRepositoryImpl {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public PointsRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public static int getCategoryPoints(@NonNull WasteCategory category) {
        switch (category) {
            case PAPER:   return 20;
            case PLASTIC: return 40;
            case GLASS:   return 60;
            case METAL:   return 60;
            case EWASTE:  return 40;
            case ORGANIC: return 20;
            case GENERAL: return 40;
            default:      return 40;
        }
    }

    public interface AwardScanCallback {
        void onSuccess(int basePoints, int questBonusPoints, int totalEarnedPoints, @NonNull List<Quest> completedQuests);
        void onError(@NonNull String error);
    }

    public void awardScanPoints(@NonNull Context context,
                                @NonNull WasteCategory category,
                                @NonNull AwardScanCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated.");
            return;
        }

        int basePoints = getCategoryPoints(category);
        String todayDate = QuestRepository.getTodayDateKey();
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            Long balance = snapshot.getLong("totalPoints");
            Long items = snapshot.getLong("itemsRecycled");
            Long streak = snapshot.getLong("currentStreak");
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalPoints", (balance == null ? 0L : balance) + basePoints);
            updates.put("lifetimePoints", com.pinduoduo.trashgo.util.PointTotals.afterEarning(
                    snapshot.getLong("lifetimePoints"), balance, basePoints));
            updates.put("itemsRecycled", (items == null ? 0L : items) + 1);
            updates.put("currentStreak", calculateStreak(snapshot.getString("lastScanDate"),
                    todayDate, streak == null ? 0L : streak));
            updates.put("lastScanDate", todayDate);
            transaction.set(userRef, updates, com.google.firebase.firestore.SetOptions.merge());
            return basePoints;
        }).addOnSuccessListener(points -> callback.onSuccess(points, 0, points,
                java.util.Collections.emptyList()))
          .addOnFailureListener(e -> callback.onError("Failed to save drop-off points. Please retry."));
    }

    private long calculateStreak(String lastScanDate, String todayDate, long currentStreak) {
        if (lastScanDate == null || lastScanDate.isEmpty()) {
            return 1;
        }
        if (lastScanDate.equals(todayDate)) {
            return currentStreak == 0 ? 1 : currentStreak;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date last = sdf.parse(lastScanDate);
            Date today = sdf.parse(todayDate);
            if (last != null && today != null) {
                long diffDays = (today.getTime() - last.getTime()) / (24 * 60 * 60 * 1000);
                if (diffDays == 1) {
                    return currentStreak + 1;
                }
            }
        } catch (Exception ignored) {}
        return 1;
    }

    public interface ActionCallback {
        void onSuccess(@NonNull String message, int newBalance);
        void onError(@NonNull String error);
    }

    public void redeemVoucher(@NonNull String voucherTitle, int pointsCost, @NonNull ActionCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated.");
            return;
        }

        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        db.runTransaction(transaction -> {
            DocumentSnapshot doc = transaction.get(userRef);
            Long balance = doc.getLong("totalPoints");
            long newBalance = com.pinduoduo.trashgo.util.PointTotals.afterSpending(balance, pointsCost);
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalPoints", newBalance);

            updates.put("lifetimePoints", com.pinduoduo.trashgo.util.PointTotals.lifetime(
                    doc.getLong("lifetimePoints"), balance));
            transaction.set(userRef, updates, com.google.firebase.firestore.SetOptions.merge());
            return Math.toIntExact(newBalance);
        }).addOnSuccessListener(balance -> callback.onSuccess("Successfully redeemed: " + voucherTitle, balance))
          .addOnFailureListener(e -> callback.onError(e.getMessage() == null
                  ? "Failed to redeem voucher. Try again." : e.getMessage()));
    }

    public void claimVoucherCode(@NonNull String code, @NonNull ActionCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated.");
            return;
        }

        String cleanCode = code.trim().toUpperCase(Locale.US);
        int bonusPoints = 0;
        if (cleanCode.equals("WELCOME100")) {
            bonusPoints = 100;
        } else if (cleanCode.equals("ECO2026")) {
            bonusPoints = 150;
        } else if (cleanCode.equals("TRASH50")) {
            bonusPoints = 50;
        } else if (cleanCode.equals("GREENGO")) {
            bonusPoints = 80;
        } else {
            callback.onError("Invalid voucher code! Please check the code and try again.");
            return;
        }

        final int pointsToAdd = bonusPoints;
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        db.runTransaction(transaction -> {
            DocumentSnapshot doc = transaction.get(userRef);
            Long balance = doc.getLong("totalPoints");
            long newBalance = com.pinduoduo.trashgo.util.PointTotals.balance(balance) + pointsToAdd;
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalPoints", newBalance);
            updates.put("lifetimePoints", com.pinduoduo.trashgo.util.PointTotals.afterEarning(
                    doc.getLong("lifetimePoints"), balance, pointsToAdd));
            transaction.set(userRef, updates, com.google.firebase.firestore.SetOptions.merge());
            return Math.toIntExact(newBalance);
        }).addOnSuccessListener(balance -> callback.onSuccess(
                "Voucher code claimed! +" + pointsToAdd + " points added.", balance))
          .addOnFailureListener(e -> callback.onError("Failed to claim voucher code."));
    }
}
