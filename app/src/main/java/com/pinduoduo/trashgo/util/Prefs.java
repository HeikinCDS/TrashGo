package com.pinduoduo.trashgo.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public final class Prefs {

    private static final String FILE = "trashgo_settings";

    private static final String KEY_SPLASH_SOUND = "splash_sound";
    private static final String KEY_USE_MILES = "use_miles";

    private Prefs() {
    }

    private static SharedPreferences prefs(@NonNull Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }


    public static boolean splashSound(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_SPLASH_SOUND, true);
    }

    public static void setSplashSound(@NonNull Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_SPLASH_SOUND, enabled).apply();
    }



    public static boolean useMiles(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_USE_MILES, false);
    }

    public static void setUseMiles(@NonNull Context context, boolean useMiles) {
        prefs(context).edit().putBoolean(KEY_USE_MILES, useMiles).apply();
        GeoUtils.setImperial(useMiles);
    }

    public static void apply(@NonNull Context context) {
        GeoUtils.setImperial(useMiles(context));
    }
}