package com.pinduoduo.trashgo.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
    private final QuestRepository questRepository;

    public PointsRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.questRepository = new QuestRepository();
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
        List<Quest> completedQuests = questRepository.onWasteScanned(context, category);

        int questBonusPoints = 0;
        for (Quest q : completedQuests) {
            questBonusPoints += q.getRewardPoints();
        }

        int totalEarned = basePoints + questBonusPoints;
        String uid = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        final int finalQuestBonus = questBonusPoints;
        final int finalTotalEarned = totalEarned;

        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                // If user document creation fallback
                Map<String, Object> newUserData = new HashMap<>();
                newUserData.put("uid", uid);
                newUserData.put("displayName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
                newUserData.put("email", currentUser.getEmail());
                newUserData.put("totalPoints", finalTotalEarned);
                newUserData.put("itemsRecycled", 1);
                newUserData.put("currentStreak", 1);
                newUserData.put("lastScanDate", todayDate);

                userRef.set(newUserData)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(basePoints, finalQuestBonus, finalTotalEarned, completedQuests))
                        .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to update profile"));
                return;
            }

            DocumentSnapshot snapshot = task.getResult();
            long currentPoints = snapshot.contains("totalPoints") && snapshot.getLong("totalPoints") != null
                    ? snapshot.getLong("totalPoints") : 0L;
            long currentItems = snapshot.contains("itemsRecycled") && snapshot.getLong("itemsRecycled") != null
                    ? snapshot.getLong("itemsRecycled") : 0L;
            long streak = snapshot.contains("currentStreak") && snapshot.getLong("currentStreak") != null
                    ? snapshot.getLong("currentStreak") : 0L;
            String lastScanDate = snapshot.getString("lastScanDate");

            long updatedStreak = calculateStreak(lastScanDate, todayDate, streak);

            Map<String, Object> updates = new HashMap<>();
            updates.put("totalPoints", currentPoints + finalTotalEarned);
            updates.put("itemsRecycled", currentItems + 1);
            updates.put("currentStreak", updatedStreak);
            updates.put("lastScanDate", todayDate);

            userRef.update(updates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(basePoints, finalQuestBonus, finalTotalEarned, completedQuests))
                    .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to update points"));
        });
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

        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                callback.onError("Failed to fetch user points.");
                return;
            }
            DocumentSnapshot doc = task.getResult();
            long currentPoints = doc.contains("totalPoints") && doc.getLong("totalPoints") != null
                    ? doc.getLong("totalPoints") : 0L;

            if (currentPoints < pointsCost) {
                callback.onError("Insufficient points balance! You need " + pointsCost + " points.");
                return;
            }

            int newBalance = (int) (currentPoints - pointsCost);
            userRef.update("totalPoints", newBalance)
                    .addOnSuccessListener(aVoid -> callback.onSuccess("Successfully redeemed: " + voucherTitle, newBalance))
                    .addOnFailureListener(e -> callback.onError("Failed to redeem voucher. Try again."));
        });
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

        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                callback.onError("Failed to connect to account.");
                return;
            }
            DocumentSnapshot doc = task.getResult();
            long currentPoints = doc.contains("totalPoints") && doc.getLong("totalPoints") != null
                    ? doc.getLong("totalPoints") : 0L;

            int newBalance = (int) (currentPoints + pointsToAdd);
            userRef.update("totalPoints", newBalance)
                    .addOnSuccessListener(aVoid -> callback.onSuccess("Voucher code claimed! +" + pointsToAdd + " points added.", newBalance))
                    .addOnFailureListener(e -> callback.onError("Failed to claim voucher code."));
        });
    }
}
