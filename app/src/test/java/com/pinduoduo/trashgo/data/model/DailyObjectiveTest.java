package com.pinduoduo.trashgo.data.model;
import org.junit.Test;
import static org.junit.Assert.*;

public class DailyObjectiveTest {
    @Test public void sameDayHasOneStableCategoryAndStatedReward() {
        Quest a = DailyObjective.forDate("2026-09-04");
        Quest b = DailyObjective.forDate("2026-09-04");
        assertEquals(a.getTargetCategory(), b.getTargetCategory());
        assertEquals(1, a.getTargetAmount());
        assertEquals(100, a.getRewardPoints());
        assertFalse(a.isCompleted());
    }
    @Test public void matchingCategoryPaysOnceAndOtherCategoriesDoNotPay() {
        for (WasteCategory category : WasteCategory.values()) {
            Quest q = new Quest("id", DailyObjective.title(category), category,
                    1, 0, 100, true, false, "date");
            assertEquals(100, DailyObjective.reward(q, category, false));
            assertEquals(0, DailyObjective.reward(q, category, true));
            assertEquals(0, DailyObjective.reward(q, null, false));
            for (WasteCategory other : WasteCategory.values()) {
                if (other != category) assertEquals(0, DailyObjective.reward(q, other, false));
            }
        }
    }
    @Test public void metalAndGlassAreNotLimitedToCansAndBottles() {
        assertEquals("Scan any metal item", DailyObjective.title(WasteCategory.METAL));
        assertEquals("Scan any glass item", DailyObjective.title(WasteCategory.GLASS));
    }
    @Test public void newDaysHaveNewObjectivesAndVariedCategories() {
        java.util.Set<WasteCategory> categories = new java.util.HashSet<>();
        for (int day = 1; day <= 28; day++)
            categories.add(DailyObjective.forDate("2026-09-" + day).getTargetCategory());
        assertTrue(categories.size() > 1);
        assertNotEquals(DailyObjective.forDate("2026-09-04").getId(),
                DailyObjective.forDate("2026-09-05").getId());
    }
}
