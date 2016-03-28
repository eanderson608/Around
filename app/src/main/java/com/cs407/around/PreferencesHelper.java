package com.cs407.around;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    // file where preferences are stored
    public static final String PREFS_NAME = "AROUND_PREFS";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferencesHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public String getPreferences(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void savePreferences(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }
}
