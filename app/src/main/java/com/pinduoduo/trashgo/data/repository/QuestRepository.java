package com.pinduoduo.trashgo.data.repository;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.pinduoduo.trashgo.data.model.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class QuestRepository {
    private static final String CLAIMS = "dailyObjectiveClaims";
    public static String getTodayDateKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
    public interface Callback {
        void onSuccess(Quest quest, int awarded);
        void onError(String message);
    }
    public List<Quest> getTodayQuests(Context context) {
        Quest quest = DailyObjective.forDate(getTodayDateKey());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) mark(quest, context.getSharedPreferences(
                "daily_objectives_" + user.getUid(), 0).getBoolean(quest.getDateKey(), false));
        return Collections.singletonList(quest);
    }
    private static void mark(Quest quest, boolean done) {
        quest.setCompleted(done);
        quest.setCurrentAmount(done ? 1 : 0);
    }
    private static void cache(Context context, String uid, Quest quest) {
        context.getSharedPreferences("daily_objectives_" + uid, 0).edit()
                .putBoolean(quest.getDateKey(), quest.isCompleted()).apply();
    }
    public void refresh(Context context, Callback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { callback.onError("Sign in to load your objective."); return; }
        Context app = context.getApplicationContext();
        Quest quest = DailyObjective.forDate(getTodayDateKey());
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    mark(quest, snapshot.get(CLAIMS + "." + quest.getDateKey()) != null);
                    cache(app, user.getUid(), quest);
                    callback.onSuccess(quest, 0);
                }).addOnFailureListener(e -> callback.onError("Unable to refresh objective."));
    }
    public void completeScan(Context context, WasteCategory category, Callback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { callback.onError("Sign in to claim your daily objective."); return; }
        Quest quest = DailyObjective.forDate(getTodayDateKey());
        if (!DailyObjective.matches(quest, category)) {
            callback.onSuccess(quest, 0);
            return;
        }
        Context app = context.getApplicationContext();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("users").document(user.getUid());
        // Persist the claim and balance together; transaction retries cannot pay twice.
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(ref);
            int reward = DailyObjective.reward(quest, category,
                    snapshot.get(CLAIMS + "." + quest.getDateKey()) != null);
            if (reward > 0) {
                Long balance = snapshot.getLong("totalPoints");
                Map<String, Object> updates = new HashMap<>();
                updates.put("totalPoints", (balance == null ? 0L : balance) + reward);
                updates.put("lifetimePoints", com.pinduoduo.trashgo.util.PointTotals.afterEarning(
                        snapshot.getLong("lifetimePoints"), balance, reward));
                updates.put(CLAIMS, Collections.singletonMap(quest.getDateKey(), reward));
                transaction.set(ref, updates, SetOptions.merge());
            }
            return reward;
        }).addOnSuccessListener(reward -> {
            mark(quest, true);
            cache(app, user.getUid(), quest);
            callback.onSuccess(quest, reward);
        }).addOnFailureListener(e -> callback.onError(
                "Objective points could not be saved. Check your connection and tap to retry."));
    }
}
