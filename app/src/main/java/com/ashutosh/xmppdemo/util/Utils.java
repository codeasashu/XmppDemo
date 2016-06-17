package com.ashutosh.xmppdemo.util;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class Utils {

    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");

    public static String getCurrentTime() {

        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    public static String getCurrentDate() {

        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static String getStatusMode(int status) {
        String mPresenceMode = "";
        switch (status){
            case Constants.PRESENCE_MODE_AVAILABLE_INT:
                mPresenceMode =  Constants.PRESENCE_MODE_AVAILABLE;
                break;
            case Constants.PRESENCE_MODE_AWAY_INT:
                mPresenceMode =  Constants.PRESENCE_MODE_AWAY;
                break;
            case Constants.PRESENCE_MODE_DND_INT:
                mPresenceMode =  Constants.PRESENCE_MODE_DND;
                break;
            case Constants.PRESENCE_MODE_OFFLINE_INT:
                mPresenceMode =  Constants.PRESENCE_MODE_XA;
                break;
            default:
                mPresenceMode = Constants.PRESENCE_MODE_XA;
                break;
        }

        return mPresenceMode;
    }

    public static String getPresenceMode (Presence.Mode mode) {
        String mPresenceMode = "";
        switch (mode){
            case available:
                mPresenceMode =  Constants.PRESENCE_MODE_AVAILABLE;
                break;
            case away:
                mPresenceMode =  Constants.PRESENCE_MODE_AWAY;
                break;
            case chat:
                mPresenceMode =  Constants.PRESENCE_MODE_CHAT;
                break;
            case dnd:
                mPresenceMode =  Constants.PRESENCE_MODE_DND;
                break;
            case xa:
                mPresenceMode = Constants.PRESENCE_MODE_XA;
                break;
            default:
                mPresenceMode = Constants.PRESENCE_MODE_XA;
                break;
        }

        return mPresenceMode;
    }

    public static String getChatMode (ChatState mode) {
        String mChatMode = "";
        switch (mode){
            case composing:
                mChatMode =  Constants.CHAT_MODE_TYPING;
                break;
            case gone:
                mChatMode =  Constants.CHAT_MODE_LEFT;
                break;
            case paused:
                mChatMode =  Constants.CHAT_MODE_PAUSED;
                break;
            case active:
                mChatMode =  Constants.CHAT_MODE_ACTIVE;
                break;
        }

        return mChatMode;
    }
}
