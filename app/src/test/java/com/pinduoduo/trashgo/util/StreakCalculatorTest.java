package com.pinduoduo.trashgo.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StreakCalculatorTest {

    @Test
    public void firstEverScanStartsAtOne() {
        assertEquals(1, StreakCalculator.next(null, "2026-09-01", 0));
        assertEquals(1, StreakCalculator.next("", "2026-09-01", 0));
    }

    @Test
    public void secondScanSameDayDoesNotDoubleCount() {
        assertEquals(4, StreakCalculator.next("2026-09-01", "2026-09-01", 4));
    }

    @Test
    public void scanYesterdayExtendsTheStreak() {
        assertEquals(5, StreakCalculator.next("2026-08-31", "2026-09-01", 4));
    }

    @Test
    public void missingADayResetsTheStreak() {
        assertEquals(1, StreakCalculator.next("2026-08-30", "2026-09-01", 9));
    }

    @Test
    public void handlesMonthBoundary() {
        assertEquals(2, StreakCalculator.next("2026-08-31", "2026-09-01", 1));
    }

    @Test
    public void handlesYearBoundary() {
        assertEquals(8, StreakCalculator.next("2025-12-31", "2026-01-01", 7));
    }

    @Test
    public void handlesLeapDay() {
        assertEquals(3, StreakCalculator.next("2028-02-29", "2028-03-01", 2));
        assertEquals(3, StreakCalculator.next("2028-02-28", "2028-02-29", 2));
    }

    @Test
    public void recoversFromCorruptStoredDate() {
        assertEquals(1, StreakCalculator.next("not-a-date", "2026-09-01", 6));
        assertEquals(1, StreakCalculator.next("2026-09-05", "2026-09-01", 6));
    }
}