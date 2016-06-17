package com.ashutosh.xmppdemo;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.ashutosh.xmppdemo.util.Constants;
import com.ashutosh.xmppdemo.xmpp.LocalBinder;
import com.ashutosh.xmppdemo.xmpp.XMPPEventReceiver;
import com.ashutosh.xmppdemo.xmpp.XMPPService;

public class ChatApplication extends Application implements Application.ActivityLifecycleCallbacks{

    private final String TAG = getClass().getSimpleName();
    private static ChatApplication mInstance = null;

    public static XMPPService xmppService;
    public Boolean mBounded = false;

    //Our broadCast receive to update us on various events
    private XMPPEventReceiver mEventReceiver;

    /*
     * We need a service connection because we are starting XMPP service with `bindService` instead of `startService`.
     * BindService is used whenever your activity needs a back and forth communication with your service, as opposed to
     * Startservice, where you activity and services are not in communication
     *
     * Bindservice communicates with your activity using ServiceConnection, which is why you have this block of code below.
     * ReadMore: https://developer.android.com/guide/components/bound-services.html#Binder
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(final ComponentName name,
                                       final IBinder service) {
            xmppService = ((LocalBinder<XMPPService>) service).getService();
            mBounded = true;
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            xmppService = null;
            mBounded = false;
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        if (mEventReceiver == null) mEventReceiver = new XMPPEventReceiver();

        IntentFilter intentFilter = new IntentFilter(Constants.EVT_LOGGED_IN);
        intentFilter.addAction(Constants.EVT_SIGNUP_SUC);
        intentFilter.addAction(Constants.EVT_SIGNUP_ERR);
        intentFilter.addAction(Constants.EVT_NEW_MSG);
        intentFilter.addAction(Constants.EVT_AUTH_SUC);
        intentFilter.addAction(Constants.EVT_RECONN_ERR);
        intentFilter.addAction(Constants.EVT_RECONN_WAIT);
        intentFilter.addAction(Constants.EVT_RECONN_SUC);
        intentFilter.addAction(Constants.EVT_CONN_SUC);
        intentFilter.addAction(Constants.EVT_CONN_CLOSE);
        intentFilter.addAction(Constants.EVT_LOGIN_ERR);
        intentFilter.addAction(Constants.EVT_PRESENCE_CHG);
        intentFilter.addAction(Constants.EVT_CHATSTATE_CHG);
        intentFilter.addAction(Constants.EVT_REQUEST_SUBSCRIBE);

        registerReceiver(mEventReceiver, intentFilter);
    }

    public XMPPEventReceiver getEventReceiver(){
        return mEventReceiver;
    }



    public static synchronized ChatApplication getInstance() {
        return mInstance;
    }

    public void BindService(Intent intent) {
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void UnbindService() {
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    public static XMPPService getmService() {
        return xmppService;
    }

    //LifeCycle Methods

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (mEventReceiver != null) unregisterReceiver(mEventReceiver);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
