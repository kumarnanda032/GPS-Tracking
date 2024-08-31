package com.example.gpstracking;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class preferences {
    private static final String DATA_LOGIN = "status_login";
    private static final String DATA_ROLE = "user_role";

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setDataLogin(Context context, boolean status) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(DATA_LOGIN, status);
        editor.apply();
    }

    public static boolean getDataLogin(Context context) {
        return getSharedPreferences(context).getBoolean(DATA_LOGIN, false);
    }

    public static void setUserRole(Context context, String role) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(DATA_ROLE, role);
        editor.apply();
    }

    public static String getUserRole(Context context) {
        return getSharedPreferences(context).getString(DATA_ROLE, "user"); // Default to "user" if not set
    }

    public static void clearData(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(DATA_LOGIN);
        editor.remove(DATA_ROLE);
        editor.apply();
    }
}
