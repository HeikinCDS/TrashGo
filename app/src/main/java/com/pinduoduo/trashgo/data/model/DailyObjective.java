package com.pinduoduo.trashgo.data.model;

import java.util.Random;

public final class DailyObjective {
    private DailyObjective() {}
    public static Quest forDate(String date) {
        WasteCategory[] pool = {WasteCategory.PAPER, WasteCategory.PLASTIC, WasteCategory.GLASS,
                WasteCategory.METAL, WasteCategory.EWASTE, WasteCategory.ORGANIC};
        WasteCategory category = pool[new Random(date.hashCode()).nextInt(pool.length)];
        return new Quest("obj_" + date, title(category), category, 1, 0, 100, true, false, date);
    }
    public static String title(WasteCategory category) {
        switch (category) {
            case PAPER: return "Scan any paper item";
            case PLASTIC: return "Scan any plastic item";
            case GLASS: return "Scan any glass item";
            case METAL: return "Scan any metal item";
            case EWASTE: return "Scan any electronic waste item";
            case ORGANIC: return "Scan any organic waste item";
            default: return "Scan any general waste item";
        }
    }
    public static boolean matches(Quest quest, WasteCategory scanned) {
        return scanned != null && quest.getTargetCategory() == scanned;
    }
    public static int reward(Quest quest, WasteCategory scanned, boolean claimed) {
        return !claimed && matches(quest, scanned) ? quest.getRewardPoints() : 0;
    }
}
