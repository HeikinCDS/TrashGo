package com.pinduoduo.trashgo.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pinduoduo.trashgo.data.model.MyVoucher;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyVoucherManager {

    private static final String PREF_NAME = "trashgo_my_vouchers_pref";
    private static final String KEY_VOUCHERS = "claimed_vouchers_json";
    private static final Gson gson = new Gson();

    @NonNull
    public static synchronized List<MyVoucher> getClaimedVouchers(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_VOUCHERS, null);
        if (json != null && !json.trim().isEmpty()) {
            try {
                Type type = new TypeToken<List<MyVoucher>>() {}.getType();
                List<MyVoucher> list = gson.fromJson(json, type);
                if (list != null) return list;
            } catch (Exception ignored) {}
        }
        return new ArrayList<>();
    }

    public static synchronized void addClaimedVoucher(@NonNull Context context,
                                                       @NonNull String title,
                                                       @NonNull String code,
                                                       int pointsSpent) {
        List<MyVoucher> current = getClaimedVouchers(context);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String id = "my_v_" + System.currentTimeMillis();

        MyVoucher voucher = new MyVoucher(id, title, code, dateStr, pointsSpent);
        current.add(0, voucher); // Latest first

        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_VOUCHERS, gson.toJson(current)).apply();
    }
}
