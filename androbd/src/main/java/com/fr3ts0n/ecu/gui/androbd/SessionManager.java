package com.fr3ts0n.ecu.gui.androbd;

import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final String PREF_NAME = "AppSession";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String KEY_USERNAME = "Username";
    private static final String KEY_USER_ID = "UserId";
    private static final String KEY_ROLE = "Role";

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String username, int userId, String role) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.commit();
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, null);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }
}
