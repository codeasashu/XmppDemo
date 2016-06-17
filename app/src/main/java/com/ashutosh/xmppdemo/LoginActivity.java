package com.ashutosh.xmppdemo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ashutosh.xmppdemo.util.SessionManager;
import com.ashutosh.xmppdemo.xmpp.XMPPHandler;
import com.ashutosh.xmppdemo.xmpp.XMPPService;
import com.ashutosh.xmppdemo.xmpp.XmppCustomEventListener;


public class LoginActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private EditText mUsernameText, mPasswordText;
    private String username,password;

    private ChatApplication mChatApp = ChatApplication.getInstance();
    private SessionManager sessionManager;
    private XMPPHandler xmppHandler;

    private XmppCustomEventListener xmppCustomEventListener = new XmppCustomEventListener(){

        @Override
        public void onConnected() {
            xmppHandler = ChatApplication.getmService().xmpp;
            xmppHandler.setUserPassword(username,password);
            xmppHandler.login();
        }

        //Event Listeners
        public void onLoggedIn(){

            //Save current User
            sessionManager.saveCurrentUser( ChatApplication.getmService().xmpp.getCurrentUserDetails() );

            Intent chatListIntent = new Intent(LoginActivity.this,ChatListActivity.class);
            startActivity(chatListIntent);
            finish();
        }

        public void onLoginFailed(){
            xmppHandler.disconnect();
            Toast.makeText(getApplicationContext(),getString(R.string.login_failed),Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());

        mChatApp.getEventReceiver().setListener(xmppCustomEventListener);

        mUsernameText = (EditText) findViewById(R.id.username);
        mPasswordText = (EditText) findViewById(R.id.password);

        findViewById(R.id.signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupIntent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(signupIntent);
            }
        });


        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = mUsernameText.getText().toString().trim();
                password = mPasswordText.getText().toString().trim();

                if( username.isEmpty() || password.isEmpty() ){
                    Toast.makeText(getApplicationContext(),getString(R.string.blank_login),Toast.LENGTH_SHORT).show();
                    return;
                }

                startXmppService();

            }
        });
    }

    private void startXmppService() {

        //Start XMPP Service (if not running already)
        if( !XMPPService.isServiceRunning ){
            Intent intent = new Intent(this, XMPPService.class);
            mChatApp.UnbindService();
            mChatApp.BindService(intent);
        } else {
            xmppHandler = ChatApplication.getmService().xmpp;
            if(!xmppHandler.isConnected()){
                xmppHandler.connect();
            } else {
                xmppHandler.setUserPassword(username,password);
                xmppHandler.login();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.code:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getString(R.string.codeurl)));
                startActivity(intent);
                return true;
            case R.id.about:
                showAboutDialog();
                return true;
            default:break;
        }

        return true;
    }

    private void showAboutDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.about_dialog, null);

        dialog.setContentView(layout);

        TextView aboutBodyView = (TextView) dialog.findViewById(R.id.about_body);
        aboutBodyView.setText(Html.fromHtml(getString(R.string.about_body)));
        aboutBodyView.setMovementMethod(new LinkMovementMethod());

        Button okBtn = (Button) dialog.findViewById(R.id.okbtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChatApp.getEventReceiver().setListener(xmppCustomEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
