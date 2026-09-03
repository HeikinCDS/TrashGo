package com.pinduoduo.trashgo.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pinduoduo.trashgo.data.model.Quest;
import com.pinduoduo.trashgo.data.model.WasteCategory;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class QuestRepository {

    private static final String PREF_NAME = "trashgo_quests_prefs";
    private static final String KEY_LAST_DATE = "last_quest_date";
    private static final String KEY_QUESTS_JSON = "quests_json";

    private final Gson gson = new Gson();

    public static String getTodayDateKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    @NonNull
    public synchronized List<Quest> getTodayQuests(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String todayKey = getTodayDateKey();
        String lastDate = prefs.getString(KEY_LAST_DATE, "");
        String json = prefs.getString(KEY_QUESTS_JSON, null);

        if (todayKey.equals(lastDate) && json != null && !json.trim().isEmpty()) {
            try {
                Type type = new TypeToken<List<Quest>>() {}.getType();
                List<Quest> cachedQuests = gson.fromJson(json, type);
                if (cachedQuests != null && !cachedQuests.isEmpty()) {
                    List<Quest> objectivesOnly = new ArrayList<>();
                    for (Quest q : cachedQuests) {
                        if (q.isObjectiveOfDay()) {
                            objectivesOnly.add(q);
                        }
                    }
                    if (!objectivesOnly.isEmpty()) {
                        saveQuests(context, todayKey, objectivesOnly);
                        return objectivesOnly;
                    }
                }
            } catch (Exception e) {
                // Fallback to regenerate
            }
        }

        List<Quest> newQuests = generateQuestsForDate(todayKey);
        saveQuests(context, todayKey, newQuests);
        return newQuests;
    }

    private void saveQuests(@NonNull Context context, @NonNull String dateKey, @NonNull List<Quest> quests) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(quests);
        prefs.edit()
                .putString(KEY_LAST_DATE, dateKey)
                .putString(KEY_QUESTS_JSON, json)
                .apply();
    }

    private List<Quest> generateQuestsForDate(String dateKey) {
        List<Quest> quests = new ArrayList<>();
        Random random = new Random(dateKey.hashCode());

        // Pool for Objective of the Day (100 pts)
        QuestOption[] objectivePool = new QuestOption[]{
                new QuestOption("Collect and throw 5 Paper", WasteCategory.PAPER, 5, 100),
                new QuestOption("Collect and throw 4 Plastic items", WasteCategory.PLASTIC, 4, 100),
                new QuestOption("Collect and throw 3 Glass bottles", WasteCategory.GLASS, 3, 100),
                new QuestOption("Collect and throw 3 Metal cans", WasteCategory.METAL, 3, 100),
                new QuestOption("Collect and throw 4 E-Waste items", WasteCategory.EWASTE, 4, 100),
                new QuestOption("Collect and throw 3 Organic waste items", WasteCategory.ORGANIC, 3, 100)
        };

        // Pick 1 Objective of the Day
        QuestOption selectedObjective = objectivePool[random.nextInt(objectivePool.length)];
        quests.add(new Quest(
                "obj_" + dateKey,
                selectedObjective.title,
                selectedObjective.category,
                selectedObjective.targetAmount,
                0,
                selectedObjective.rewardPoints,
                true,
                false,
                dateKey
        ));

        return quests;
    }

    /**
     * Advances quest progress when waste is scanned. Returns list of newly completed quests.
     */
    @NonNull
    public synchronized List<Quest> onWasteScanned(@NonNull Context context, @NonNull WasteCategory category) {
        List<Quest> currentQuests = getTodayQuests(context);
        List<Quest> newlyCompleted = new ArrayList<>();
        boolean updated = false;

        for (Quest q : currentQuests) {
            if (!q.isCompleted()) {
                if (q.getTargetCategory() == category || q.getTargetCategory() == WasteCategory.GENERAL) {
                    q.setCurrentAmount(q.getCurrentAmount() + 1);
                    updated = true;
                    if (q.getCurrentAmount() >= q.getTargetAmount()) {
                        q.setCompleted(true);
                        newlyCompleted.add(q);
                    }
                }
            }
        }

        if (updated) {
            saveQuests(context, getTodayDateKey(), currentQuests);
        }

        return newlyCompleted;
    }

    private static class QuestOption {
        String title;
        WasteCategory category;
        int targetAmount;
        int rewardPoints;

        QuestOption(String title, WasteCategory category, int targetAmount, int rewardPoints) {
            this.title = title;
            this.category = category;
            this.targetAmount = targetAmount;
            this.rewardPoints = rewardPoints;
        }
    }
}
