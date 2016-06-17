package com.ashutosh.xmppdemo.xmpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ashutosh.xmppdemo.model.ChatItem;
import com.ashutosh.xmppdemo.util.Constants;
import com.ashutosh.xmppdemo.model.ChatStateModel;
import com.ashutosh.xmppdemo.model.PresenceModel;



public class XMPPEventReceiver extends BroadcastReceiver {

    XmppCustomEventListenerBase xmppCustomEventListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        switch ( intent.getAction() ){

            case Constants.EVT_LOGGED_IN:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onLoggedIn();
                break;

            case Constants.EVT_SIGNUP_SUC:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onSignupSuccess();
                break;

            case Constants.EVT_SIGNUP_ERR:
                Bundle intentBundle2 = intent.getExtras();
                String signupError = intentBundle2.getString( Constants.INTENT_KEY_SIGNUP_ERR );
                if(xmppCustomEventListener != null) xmppCustomEventListener.onSignupFailed(signupError);
                break;

            case Constants.EVT_AUTH_SUC:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onAuthenticated();
                break;

            case Constants.EVT_RECONN_ERR:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onReConnectionError();
                break;

            case Constants.EVT_RECONN_WAIT:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onReConnection();
                break;

            case Constants.EVT_RECONN_SUC:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onReConnected();
                break;

            case Constants.EVT_CONN_SUC:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onConnected();
                break;

            case Constants.EVT_CONN_CLOSE:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onConnectionClosed();
                break;

            case Constants.EVT_LOGIN_ERR:
                if(xmppCustomEventListener != null) xmppCustomEventListener.onLoginFailed();
                break;

            case Constants.EVT_NEW_MSG:
                Bundle data = intent.getExtras();
                ChatItem chatItem = (ChatItem) data.getParcelable( Constants.INTENT_KEY_NEWMSG );
                if(xmppCustomEventListener != null) xmppCustomEventListener.onNewMessageReceived(chatItem);
                break;

            case Constants.EVT_PRESENCE_CHG:
                Bundle bundle = intent.getExtras();
                PresenceModel presenceModel = (PresenceModel) bundle.getParcelable( Constants.INTENT_KEY_PRESENCE );
                if(xmppCustomEventListener != null) xmppCustomEventListener.onPresenceChanged(presenceModel);
                break;

            case Constants.EVT_CHATSTATE_CHG:
                Bundle intentBundle = intent.getExtras();
                ChatStateModel chatStateModel = (ChatStateModel) intentBundle.getParcelable( Constants.INTENT_KEY_CHATSTATE );
                if(xmppCustomEventListener != null) xmppCustomEventListener.onChatStateChanged(chatStateModel);
                break;

            case Constants.EVT_REQUEST_SUBSCRIBE:
                Bundle intentBundle1 = intent.getExtras();
                String fromUserID = intentBundle1.getString( Constants.INTENT_KEY_NEWREQUEST );
                if(xmppCustomEventListener != null) xmppCustomEventListener.onSubscriptionRequest(fromUserID);
                break;

        }
    }

    public void setListener(XmppCustomEventListener listener) {
        this.xmppCustomEventListener = listener;
    }

    public interface XmppCustomEventListenerBase{

        void onNewMessageReceived(ChatItem chatItem);
        void onLoggedIn();
        void onSignupSuccess();
        void onSignupFailed(String error);
        void onAuthenticated();
        void onReConnectionError();
        void onConnected();
        void onReConnected();
        void onConnectionClosed();
        void onReConnection();
        void onLoginFailed();
        void onPresenceChanged(PresenceModel presenceModel);
        void onChatStateChanged(ChatStateModel chatStateModel);
        void onSubscriptionRequest(String fromuser);

    }
}
