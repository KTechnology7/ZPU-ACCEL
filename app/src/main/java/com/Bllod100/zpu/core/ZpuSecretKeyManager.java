package com.Bllod100.zpu.core;

import android.content.Context;
import android.content.SharedPreferences;

public class ZpuSecretKeyManager {
    private static final String PREF_NAME = "zpu_security_pref";
    private static final String KEY_SECRET = "zpu_secret_key";
    private static final String DEFAULT_KEY = "ZPU-APEX-SECURE-KEY-2026";

    public static String getSecretKey(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_SECRET, DEFAULT_KEY);
        } catch (Exception e) {
            return DEFAULT_KEY;
        }
    }

    public static void setSecretKey(Context context, String key) {
        if (key != null && !key.trim().isEmpty()) {
            try {
                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(KEY_SECRET, key.trim()).apply();
            } catch (Exception ignored) {}
        }
    }

    public static boolean validateKey(Context context, String inputKey) {
        if (inputKey == null) return false;
        String actual = getSecretKey(context);
        return actual.equals(inputKey.trim());
    }
}
