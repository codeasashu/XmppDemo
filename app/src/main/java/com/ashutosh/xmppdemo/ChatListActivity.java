package com.ashutosh.xmppdemo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ashutosh.xmppdemo.adapter.ChatListAdapter;
import com.ashutosh.xmppdemo.model.PresenceModel;
import com.ashutosh.xmppdemo.model.RoasterModel;
import com.ashutosh.xmppdemo.xmpp.XMPPEventReceiver;
import com.ashutosh.xmppdemo.xmpp.XMPPHandler;
import com.ashutosh.xmppdemo.xmpp.XmppCustomEventListener;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity {

    public final String TAG = getClass().getSimpleName();

    private RecyclerView userListView;
    private ChatListAdapter mChatListAdapter;
    private ArrayList<RoasterModel> mUserList;

    ChatApplication mChatApp = ChatApplication.getInstance();

    XMPPHandler xmppHandler;
    XMPPEventReceiver xmppEventReceiver;

    public XmppCustomEventListener xmppCustomEventListener = new XmppCustomEventListener(){

        //On User Presence Changed
        public void onPresenceChanged(PresenceModel presenceModel) {

            for(int i =0; i < mUserList.size(); i++){

                RoasterModel model = mUserList.get(i);
                if( ChatApplication.getmService().xmpp.checkSender(model,presenceModel.getUser())){
                    model.setStatus(presenceModel.getUserStatus());
                    mUserList.set(i, model);
                    break;
                }
            }
            mChatListAdapter.notifyDataSetChanged();
        }

        public void onSubscriptionRequest(final String fromUserID){
            Log.e("lol","New request - "+ fromUserID);

            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatListActivity.this, R.style.myDialog));
            AlertDialog alert = builder.create();
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
        setContentView(R.layout.activity_chat_list);

        FloatingActionButton sendReqBtn = (FloatingActionButton) findViewById(R.id.send_request);
        FloatingActionButton viewReqBtn = (FloatingActionButton) findViewById(R.id.view_request);
        FloatingActionButton logoutBtn = (FloatingActionButton) findViewById(R.id.logout);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xmppHandler.disconnect();
                Intent logoutIntent = new Intent(ChatListActivity.this,LoginActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        });

        sendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addUserIntent = new Intent(ChatListActivity.this,AddUserActivity.class);
                startActivity(addUserIntent);
            }
        });

        viewReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pendingListIntent = new Intent(ChatListActivity.this,PendingRequestsActivity.class);
                startActivity(pendingListIntent);
            }
        });

        xmppHandler = ChatApplication.getmService().xmpp;
        mUserList = xmppHandler.getOnlineUsers();

        xmppEventReceiver = mChatApp.getEventReceiver();


        mChatListAdapter = new ChatListAdapter(getApplicationContext(),mUserList);
        userListView = (RecyclerView) findViewById(R.id.user_list);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        userListView.setLayoutManager(mLayoutManager);
        userListView.setItemAnimator(new DefaultItemAnimator());
        userListView.setAdapter(mChatListAdapter);

        mChatListAdapter.notifyDataSetChanged();
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
        xmppEventReceiver.setListener(xmppCustomEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatApp.UnbindService();
    }
}
