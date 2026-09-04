package com.pinduoduo.trashgo.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PointTotalsTest {
    @Test public void legacyAccountStartsFromExistingBalance() {
        assertEquals(500L, PointTotals.lifetime(null, 500L));
        assertEquals(600L, PointTotals.afterEarning(null, 500L, 100));
    }
    @Test public void spendingDoesNotReduceLifetime() {
        long earned = PointTotals.lifetime(null, 500L);
        long balance = PointTotals.afterSpending(500L, 200);
        assertEquals(300L, balance);
        assertEquals(500L, PointTotals.lifetime(earned, balance));
        assertEquals(600L, PointTotals.afterEarning(earned, balance, 100));
    }
    @Test public void newAccountStartsAtZero() {
        assertEquals(0L, PointTotals.lifetime(null, null));
        assertEquals(100L, PointTotals.afterEarning(0L, 0L, 100));
    }
    @Test(expected = IllegalArgumentException.class)
    public void cannotOverspend() { PointTotals.afterSpending(100L, 200); }
    @Test(expected = IllegalArgumentException.class)
    public void cannotRedeemNegativeCost() { PointTotals.afterSpending(100L, -1); }
}
