package com.gogrocerdb.tcc.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


import com.gogrocerdb.tcc.Activity.LogInActivity;

import java.util.HashMap;

import static com.gogrocerdb.tcc.Config.BaseURL.IS_LOGIN;
import static com.gogrocerdb.tcc.Config.BaseURL.KEY_ID;
import static com.gogrocerdb.tcc.Config.BaseURL.KEY_NAME;
import static com.gogrocerdb.tcc.Config.BaseURL.PREFS_NAME;
import static com.gogrocerdb.tcc.Config.BaseURL.PREFS_NAME2;


public class Session_management {

    SharedPreferences prefs;
    SharedPreferences prefs2;

    SharedPreferences.Editor editor;
    SharedPreferences.Editor editor2;
    Context context;
    int PRIVATE_MODE = 0;

    public Session_management(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, PRIVATE_MODE);
        editor = prefs.edit();
        prefs2 = context.getSharedPreferences(PREFS_NAME2, PRIVATE_MODE);
        editor2 = prefs2.edit();
    }

    //Store Data
    public void createLoginSession(String id, String name) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        editor.commit();
    }

    public void checkLogin() {

        if (!this.isLoggedIn()) {
            Intent loginsucces = new Intent(context, LogInActivity.class);
            loginsucces.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            loginsucces.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(loginsucces);
        }
    }

    //Store And Use Data

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_ID, prefs.getString(KEY_ID, null));
        user.put(KEY_NAME, prefs.getString(KEY_NAME, null));
        return user;
    }



    public void logoutSession() {
        editor.clear();
        editor.commit();
        Intent logout = new Intent(context, LogInActivity.class);
        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        logout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(logout);
    }

    // Get Login State
    public boolean isLoggedIn() {
        return prefs.getBoolean(IS_LOGIN, false);
    }

}
