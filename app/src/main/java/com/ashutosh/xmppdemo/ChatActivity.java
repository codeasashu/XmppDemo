package com.ashutosh.xmppdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ashutosh.xmppdemo.adapter.ChatAdapter;
import com.ashutosh.xmppdemo.model.ChatItem;
import com.ashutosh.xmppdemo.model.ChatStateModel;
import com.ashutosh.xmppdemo.model.PresenceModel;
import com.ashutosh.xmppdemo.model.RoasterModel;
import com.ashutosh.xmppdemo.util.Constants;
import com.ashutosh.xmppdemo.util.SessionManager;
import com.ashutosh.xmppdemo.util.Utils;
import com.ashutosh.xmppdemo.xmpp.XMPPEventReceiver;
import com.ashutosh.xmppdemo.xmpp.XMPPHandler;
import com.ashutosh.xmppdemo.xmpp.XmppCustomEventListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements TextWatcher{

    public static final String TAG = ChatActivity.class.getSimpleName();
    //My jabber username.
    public String user1;
    public String user2;

    //Ui Elements
    private ListView chatListView;
    private EditText msgEditText;
    private TextView chatStatusTv;


    public static ArrayList<ChatItem> chatItemArrayList;
    public static ChatAdapter chatAdapter;

    final Handler mHandler = new Handler();

    //Chat App to start services etc
    ChatApplication mChatApp = ChatApplication.getInstance();

    //Get our custom event receiver so that we can bind our event listener to it
    XMPPEventReceiver xmppEventReceiver;
    XMPPHandler xmppHandler;
    RoasterModel roasterModel;

    SessionManager sessionManager;

    public XmppCustomEventListener xmppCustomEventListener = new XmppCustomEventListener(){

        //Event Listeners
        public void onNewMessageReceived( ChatItem chatItem ){
            chatItem.setIsMine(false);

            if( ChatApplication.getmService().xmpp.checkSender(roasterModel,chatItem.getSender())) {
                chatItemArrayList.add(chatItem);
                chatAdapter.notifyDataSetChanged();
            }
        }

        //On User Presence Changed
        public void onPresenceChanged(PresenceModel presenceModel) {
            String presence = Utils.getStatusMode(presenceModel.getUserStatus());
            getSupportActionBar().setSubtitle( presence );
        }

        //On Chat Status Changed
        public void onChatStateChanged(ChatStateModel chatStateModel) {

            String chatStatus = Utils.getChatMode(chatStateModel.getChatState());

            if( ChatApplication.getmService().xmpp.checkSender(roasterModel,chatStateModel.getUser())) {
                chatStatusTv.setText(chatStatus);
            }
        }

        public void onSubscriptionRequest(final String fromUserID){
            Log.e("lol","New request - "+ fromUserID);

            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));
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
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(getApplicationContext());

        xmppEventReceiver = mChatApp.getEventReceiver();

        Intent intent = getIntent();

        if( sessionManager.getUser() != null ) {
            user1 = sessionManager.getUser();
        }

        Bundle data = intent.getExtras();
        roasterModel = (RoasterModel) data.getParcelable("roaster");
        user2 = roasterModel.getRoasterEntryUser();

        String presence = Utils.getStatusMode(roasterModel.getStatus());

        getSupportActionBar().setTitle(user2);
        getSupportActionBar().setSubtitle( presence );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        msgEditText = (EditText) findViewById(R.id.messageEditText);
        chatStatusTv = (TextView) findViewById(R.id.chat_status);
        ImageButton sendChatBtn = (ImageButton) findViewById(R.id.sendMessageButton);

        //Send message on "Send" button click
        sendChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage(v);
            }
        });

        //Setup our listView
        chatListView = (ListView) findViewById(R.id.msgListView);
        chatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); //Google why I did this
        chatListView.setStackFromBottom(true);

        chatItemArrayList = new ArrayList<>();

        chatAdapter = new ChatAdapter(getApplicationContext(),chatItemArrayList);
        chatListView.setAdapter(chatAdapter);

        msgEditText.addTextChangedListener(this);

        //User entered activity. Update status
        ChatApplication.getmService().xmpp.updateChatStatus(user2, ChatState.active);
    }

    public void sendTextMessage(View v) {
        String message = msgEditText.getEditableText().toString();
        final String currentDate = Utils.getCurrentDate();
        final String currentTime = Utils.getCurrentTime();

        if (!message.equalsIgnoreCase("")) {
            final ChatItem chatMessage = new ChatItem(message, currentDate, currentTime, user1, user2, true);
            msgEditText.setText("");
            chatItemArrayList.add(chatMessage);
            chatAdapter.notifyDataSetChanged();

            try {

                ChatApplication.getmService().xmpp.sendMessage(chatMessage);

            } catch (SmackException e){
                e.printStackTrace();
            }

        }
    }

    //Text events
    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {

        ChatApplication.getmService().xmpp.updateChatStatus(user2, ChatState.composing);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(userStoppedTyping, (Constants.PAUSE_THRESHOLD * 1000)); // 2 second
    }

    Runnable userStoppedTyping = new Runnable() {

        @Override
        public void run() {

            //User haven't typed for PAUSE_THRESHOLD secs, mark chat status "Paused"
            ChatApplication.getmService().xmpp.updateChatStatus(user2, ChatState.paused);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //Here we bind our event listener (XmppCustomEventListener)
        xmppEventReceiver.setListener(xmppCustomEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //User left the activity, so mark his chat status "gone"
        ChatApplication.getmService().xmpp.updateChatStatus(user2, ChatState.gone);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
