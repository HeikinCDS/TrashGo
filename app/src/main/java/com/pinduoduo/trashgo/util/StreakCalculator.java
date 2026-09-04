package com.pinduoduo.trashgo.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class StreakCalculator {

    private static final String PATTERN = "yyyy-MM-dd";

    private StreakCalculator() {}

    @NonNull
    public static String todayString() {
        return format(new Date());
    }

    @NonNull
    public static String format(@NonNull Date date) {
        return new SimpleDateFormat(PATTERN, Locale.US).format(date);
    }

    public static int next(@Nullable String lastScanDate,
                           @Nullable String today,
                           int currentStreak) {
        if (today == null) {
            return Math.max(currentStreak, 1);
        }
        if (lastScanDate == null || lastScanDate.trim().isEmpty()) {
            return 1;
        }
        String last = lastScanDate.trim();
        if (last.equals(today)) {
            return Math.max(currentStreak, 1);
        }
        String yesterday = shiftDays(today, -1);
        if (yesterday != null && yesterday.equals(last)) {
            return Math.max(currentStreak, 0) + 1;
        }
        return 1;
    }

    @Nullable
    static String shiftDays(@NonNull String date, int days) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(PATTERN, Locale.US);
            format.setLenient(false);
            Calendar calendar = Calendar.getInstance(Locale.US);
            calendar.setTime(format.parse(date));
            calendar.add(Calendar.DAY_OF_MONTH, days);
            return format.format(calendar.getTime());
        } catch (ParseException e) {
            return null;
        }
    }
}