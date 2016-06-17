package com.ashutosh.xmppdemo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "XMPPDemoPref";

    // User name (make variable public to access from outside)
    public static final String KEY_USER = "user";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void saveCurrentUser(String user){

        editor.putString(KEY_USER, user);
        // commit changes
        editor.apply();
    }

    /**
     * Get stored data specific
     * */
    public String getUser(){
        return pref.getString(KEY_USER, null);
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

}
