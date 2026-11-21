package com.example.cabinetmedical;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStore {
    private static final String PREFS = "cabinet_prefs";
    private static final String KEY_PWD = "password";
    private static final String KEY_DOCTOR = "doctor_name";
    public static final String DEFAULT_PASSWORD = "123456";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getPassword(Context context) {
        return prefs(context).getString(KEY_PWD, DEFAULT_PASSWORD);
    }

    public static void setPassword(Context context, String password) {
        prefs(context).edit().putString(KEY_PWD, password).apply();
    }

    public static String getDoctor(Context context) {
        return prefs(context).getString(KEY_DOCTOR, "");
    }

    public static void setDoctor(Context context, String name) {
        prefs(context).edit().putString(KEY_DOCTOR, name).apply();
    }
}
