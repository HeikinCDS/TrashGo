package com.pinduoduo.trashgo.util;

import androidx.annotation.NonNull;
import java.util.Locale;

public final class GeoUtils {

    private static final double EARTH_RADIUS_METRES = 6_371_000d;

    private GeoUtils() { }

    /** Great-circle distance between two coordinates, in metres. */
    public static double distanceMetres(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return EARTH_RADIUS_METRES * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Formats a distance the way a person reads it: "120 m", "1.4 km". */
    @NonNull
    public static String format(double metres) {
        if (metres < 0) {
            return "—";
        }
        if (metres < 1000) {
            long rounded = Math.round(metres / 10d) * 10L;
            return rounded + " m";
        }
        return String.format(Locale.US, "%.1f km", metres / 1000d);
    }
}
