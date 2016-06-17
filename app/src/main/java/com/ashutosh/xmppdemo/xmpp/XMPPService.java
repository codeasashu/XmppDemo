package com.ashutosh.xmppdemo.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ashutosh.xmppdemo.model.ChatItem;
import com.ashutosh.xmppdemo.util.Constants;
import com.ashutosh.xmppdemo.model.ChatStateModel;
import com.ashutosh.xmppdemo.model.PresenceModel;



public class XMPPService extends Service {

    private final String TAG = getClass().getSimpleName();
    public XMPPHandler xmpp;
    public static boolean isServiceRunning = false;

    public XMPPService(){}

    @Override
    public void onCreate() {
        super.onCreate();
        xmpp = new XMPPHandler(XMPPService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(intent != null){
            xmpp.connect();
            XMPPService.isServiceRunning = true;
        }

        return new LocalBinder<>(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(xmpp != null) xmpp.disconnect();
        XMPPService.isServiceRunning = false;
    }

    public void onNewMessage(final ChatItem chatItem) {

        Intent intent = new Intent(Constants.EVT_NEW_MSG);
        intent.putExtra(Constants.INTENT_KEY_NEWMSG,chatItem);
        sendBroadcast(intent);
    }

    public void onAuthenticated() {
        sendBroadcast(new Intent(Constants.EVT_AUTH_SUC));
    }

    public void onReConnectionError() {
        sendBroadcast(new Intent(Constants.EVT_RECONN_ERR));
    }

    public void onConnected() {
        sendBroadcast(new Intent(Constants.EVT_CONN_SUC));
    }

    public void onReConnected() {
        sendBroadcast(new Intent(Constants.EVT_RECONN_SUC));
    }

    public void onConnectionClosed() {
        sendBroadcast(new Intent(Constants.EVT_CONN_CLOSE));
    }

    public void onReConnection() {
        sendBroadcast(new Intent(Constants.EVT_RECONN_WAIT));
    }

    public void onLoginFailed(){
        sendBroadcast(new Intent(Constants.EVT_LOGIN_ERR));
    }

    public void onLoggedIn(){
        sendBroadcast(new Intent(Constants.EVT_LOGGED_IN));
    }

    public void onSignupSuccess(){
        sendBroadcast(new Intent(Constants.EVT_SIGNUP_SUC));
    }

    public void onSignupFailed(String error) {
        Intent intent = new Intent(Constants.EVT_SIGNUP_ERR);
        intent.putExtra(Constants.INTENT_KEY_SIGNUP_ERR,error);
        sendBroadcast(intent);
    }

    public void onPresenceChange(PresenceModel presenceModel){
        Intent intent = new Intent(Constants.EVT_PRESENCE_CHG);
        intent.putExtra(Constants.INTENT_KEY_PRESENCE,presenceModel);
        sendBroadcast(intent);
    }

    public void onChatStateChange(ChatStateModel chatStateModel){
        Intent intent = new Intent(Constants.EVT_CHATSTATE_CHG);
        intent.putExtra(Constants.INTENT_KEY_CHATSTATE,chatStateModel);
        sendBroadcast(intent);
    }

    public void onRequestSubscribe(String fromUserID) {
        Intent intent = new Intent(Constants.EVT_REQUEST_SUBSCRIBE);
        intent.putExtra(Constants.INTENT_KEY_NEWREQUEST,fromUserID);
        sendBroadcast(intent);
    }
}
