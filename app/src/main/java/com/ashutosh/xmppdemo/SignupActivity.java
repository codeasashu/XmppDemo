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

import com.ashutosh.xmppdemo.model.SignupModel;
import com.ashutosh.xmppdemo.xmpp.XMPPHandler;
import com.ashutosh.xmppdemo.xmpp.XMPPService;
import com.ashutosh.xmppdemo.xmpp.XmppCustomEventListener;

public class SignupActivity extends AppCompatActivity {

    private EditText mUsernameText, mPasswordText, mConfirmPasswordText;
    private String username,password,confirmPassword;

    private ChatApplication mChatApp = ChatApplication.getInstance();
    private XMPPHandler xmppHandler;

    private XmppCustomEventListener xmppCustomEventListener = new XmppCustomEventListener(){

        //Event Listeners
        public void onConnected() {
            xmppHandler = ChatApplication.getmService().xmpp;
            xmppHandler.Signup(new SignupModel(username,password,confirmPassword));
        }

        public void onSignupSuccess(){
            Toast.makeText(getApplicationContext(),getString(R.string.signup_success),Toast.LENGTH_SHORT).show();

            Intent loginIntent = new Intent(SignupActivity.this,LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        public void onSignupFailed( String error ){
            xmppHandler.disconnect();
            Toast.makeText(getApplicationContext(),error,Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mChatApp.getEventReceiver().setListener(xmppCustomEventListener);

        mUsernameText = (EditText) findViewById(R.id.username);
        mPasswordText = (EditText) findViewById(R.id.password);
        mConfirmPasswordText = (EditText) findViewById(R.id.confirm_password);

        findViewById(R.id.signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = mUsernameText.getText().toString().trim();
                password = mPasswordText.getText().toString();
                confirmPassword = mConfirmPasswordText.getText().toString();

                startXmppService();

            }
        });
    }

    //Start XMPP Service
    private void startXmppService() {
        if( !XMPPService.isServiceRunning ) {
            Intent intent = new Intent(this, XMPPService.class);
            mChatApp.UnbindService();
            mChatApp.BindService(intent);
        } else {
            xmppHandler = ChatApplication.getmService().xmpp;
            if(!xmppHandler.isConnected()){
                xmppHandler.connect();
            } else {
                xmppHandler.Signup(new SignupModel(username,password,confirmPassword));
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
    protected void onDestroy() {
        super.onDestroy();
    }
}
