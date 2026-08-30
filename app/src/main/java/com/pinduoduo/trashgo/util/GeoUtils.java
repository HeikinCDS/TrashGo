package com.pinduoduo.trashgo.util;

import androidx.annotation.NonNull;

import java.util.Locale;

public final class GeoUtils {

    private static final double EARTH_RADIUS_METRES = 6_371_000d;

    private static final double FEET_PER_METRE = 3.28084d;
    private static final double METRES_PER_MILE = 1609.344d;
    private static final double FEET_SWITCH_POINT = 528d;

    private static volatile boolean imperial = false;

    private GeoUtils() {
    }

    public static void setImperial(boolean useImperial) {
        imperial = useImperial;
    }

    public static boolean isImperial() {
        return imperial;
    }

    public static double distanceMetres(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return EARTH_RADIUS_METRES * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @NonNull
    public static String format(double metres) {
        if (metres < 0) {
            return "—";
        }

        if (imperial) {
            double feet = metres * FEET_PER_METRE;
            if (feet < FEET_SWITCH_POINT) {
                long rounded = Math.round(feet / 10d) * 10L;
                return rounded + " ft";
            }
            return String.format(Locale.US, "%.1f mi", metres / METRES_PER_MILE);
        }

        if (metres < 1000) {
            long rounded = Math.round(metres / 10d) * 10L;
            return rounded + " m";
        }
        return String.format(Locale.US, "%.1f km", metres / 1000d);
    }
}