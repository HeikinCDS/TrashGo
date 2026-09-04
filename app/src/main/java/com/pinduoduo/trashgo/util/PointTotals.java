package com.pinduoduo.trashgo.util;

public final class PointTotals {
    private PointTotals() {}
    public static long balance(Long value) { return value == null ? 0L : value; }
    public static long lifetime(Long earned, Long balance) {
        return earned == null ? Math.max(0L, balance(balance)) : earned;
    }
    public static long afterEarning(Long earned, Long balance, int points) {
        if (points < 0) throw new IllegalArgumentException("Points must be positive.");
        return Math.addExact(lifetime(earned, balance), points);
    }
    public static long afterSpending(Long balance, int cost) {
        if (cost <= 0) throw new IllegalArgumentException("Invalid voucher cost.");
        if (balance(balance) < cost) throw new IllegalArgumentException("Insufficient points balance.");
        return balance(balance) - cost;
    }
}
