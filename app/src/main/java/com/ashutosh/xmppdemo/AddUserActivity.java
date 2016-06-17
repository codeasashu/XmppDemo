package com.ashutosh.xmppdemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ashutosh.xmppdemo.util.SessionManager;
import com.ashutosh.xmppdemo.xmpp.XMPPEventReceiver;
import com.ashutosh.xmppdemo.xmpp.XMPPHandler;
import com.ashutosh.xmppdemo.xmpp.XmppCustomEventListener;

public class AddUserActivity extends AppCompatActivity {

    XMPPHandler xmppHandler;
    SessionManager sessionManager;
    String user1;
    XMPPEventReceiver xmppEventReceiver;

    private ChatApplication mChatApp = ChatApplication.getInstance();

    private XmppCustomEventListener xmppCustomEventListener = new XmppCustomEventListener(){

        public void onSubscriptionRequest(final String fromUserID){

            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AddUserActivity.this, R.style.myDialog));
            builder.create();
            builder.setMessage("You got a new subscription request from: "+ fromUserID);
            builder.setPositiveButton(getString(R.string.add_request_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("lol","positive");
                    xmppHandler.confirmSubscription(fromUserID, true);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.add_request_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("lol","neg");
                    xmppHandler.confirmSubscription(fromUserID, false);
                    dialog.dismiss();
                }
            });

            builder.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adduser);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText userjid = (EditText) findViewById(R.id.jid);
        Button adduser = (Button) findViewById(R.id.adduser);

        xmppHandler = ChatApplication.getmService().xmpp;
        xmppEventReceiver = mChatApp.getEventReceiver();
        xmppEventReceiver.setListener(xmppCustomEventListener);

        sessionManager = new SessionManager(getApplicationContext());
        if( sessionManager.getUser() != null ) {
            user1 = sessionManager.getUser();
        }

        adduser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( userjid.getText().toString().isEmpty() ){
                    Toast.makeText(getApplicationContext(),getString(R.string.empty_user_jid),Toast.LENGTH_SHORT).show();
                    return;
                }
                xmppHandler.sendRequestTo(userjid.getText().toString());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        xmppEventReceiver.setListener(xmppCustomEventListener);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
