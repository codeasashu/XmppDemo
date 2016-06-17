package com.ashutosh.xmppdemo.xmpp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ashutosh.xmppdemo.model.ChatItem;
import com.ashutosh.xmppdemo.util.Constants;
import com.ashutosh.xmppdemo.model.ChatStateModel;
import com.ashutosh.xmppdemo.model.PresenceModel;
import com.ashutosh.xmppdemo.model.RoasterModel;
import com.ashutosh.xmppdemo.model.SignupModel;
import com.ashutosh.xmppdemo.util.Utils;
import com.google.gson.Gson;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.util.XmppStringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class XMPPHandler{

    private final String TAG = getClass().getSimpleName();
    public static boolean connected = false;
    public boolean loggedin = false;
    public static boolean isconnecting = false;
    public static boolean isToasted = false; //Show toast for events? set false to just print via Log
    private HashMap<String,Boolean> chat_created_for = new HashMap<>(); //for single chat env
    public static AbstractXMPPConnection connection;
    public String userId;
    public String userPassword;
    private boolean autoLogin = true;
    Roster roster;

    Gson gson;
    public XMPPService service;
    public static XMPPHandler instance = null;
    public static boolean instanceCreated = false;

    private final boolean debug = Constants.XMPP_DEBUG;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    //XMPP Stuffs
    public Chat mChat;
    private XMPPConnectionListener mConnectionListener = new XMPPConnectionListener();
    private MyChatManagerListener mChatManagerListener;
    private MyMessageListener mMessageListener;
    private MyStanzaListener mStanzaListener;
    private MyRosterListener mRoasterListener;


    /*
     * A default constructor which only service instance
     * This allows to connect, without needing to be loggedin
     * This way, we will get instance to XMPPHandler, and can login whenever we want,
     * using .login() method
     */
    public XMPPHandler(XMPPService service){
        this.service = service;
        this.autoLogin = false;

        if (instance == null) {
            instance = this;
            instanceCreated = true;
        }

        //Prepare the connections and listeners
        init();
    }

    // Get XMPPHandler instance
    public static XMPPHandler getInstance(){
        return instance;
    }

    public void init() {

        if(debug) Log.e(TAG,"starting XMPPHandler");

        gson = new Gson(); //We need GSON to parse chat messages
        mMessageListener = new MyMessageListener(); //Message event Listener
        mChatManagerListener = new MyChatManagerListener(); //Chat Manager
        mStanzaListener = new MyStanzaListener(); // Listen for incoming stanzas (packets)
        mRoasterListener = new MyRosterListener();

        // Ok, now that events have been attached, we can prepare connection
        // (we will initialize connection by calling ".connect()" method later on.
        initialiseConnection();
    }

    //Pass server address, port to initialize connection
    private void initialiseConnection() {

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration
                .builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(Constants.XMPP_DOMAIN);
        config.setHost(Constants.XMPP_HOST);
        config.setPort(Constants.XMPP_PORT);
        config.setDebuggerEnabled(Constants.XMPP_DEBUG);
        config.setResource(Constants.XMPP_RESOURCE);

        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        connection = new XMPPTCPConnection(config.build());
        connection.addConnectionListener(mConnectionListener);
        connection.addAsyncStanzaListener(mStanzaListener, new StanzaFilter() {
            @Override
            public boolean accept(Stanza stanza) {
                //You can also return only presence packets, since we are only filtering presences
                return true;
            }
        });

        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
        roster = Roster.getInstanceFor(connection);
        roster.addRosterListener(mRoasterListener);
    }

    // Set username and password explicitly for login
    public void setUserPassword(String mUsername, String mPassword) {
        this.userId = mUsername;
        this.userPassword = mPassword;
    }

    //This method sets every chat instances to false (in situations where connection closes, or error happens)
    public static void chatInstanceIterator(Map<String,Boolean> mp) {

        for (Map.Entry<String,Boolean> entry : mp.entrySet()) {
            entry.setValue(false);
        }
    }

    //check if a connection is already established
    public boolean isConnected(){
        return connected;
    }

    //Explicitly start a connection
    public void connect() {

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected synchronized Boolean doInBackground(Void... arg0) {
                //There is no point in reconnecting an already established connection. So abort, if we do
                if (connection.isConnected())
                    return false;

                //We are currently in "connection" phase, so no requests should be made while we are connecting.
                isconnecting = true;

                if (isToasted)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(service, "connecting....", Toast.LENGTH_LONG).show();
                        }
                    });

                if( debug ) Log.d(TAG, "connecting....");

                try {
                    connection.connect();

                    /**
                     * Set delivery receipt for every Message, so that we can confirm if message
                     * has been received on other end.
                     *
                     * @NOTE: This feature is not yet implemented in this example. Maybe, I'll add it later on.
                     * Feel free to pull request to add one.
                     *
                     * Read more about this: http://xmpp.org/extensions/xep-0184.html
                     **/

                    /*
                    DeliveryReceiptManager dm = DeliveryReceiptManager.getInstanceFor(connection);
                    dm.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {
                        @Override
                        public void onReceiptReceived(final String fromid,
                                                      final String toid, final String msgid,
                                                      final Stanza packet) {

                        }
                    });
                    */
                    connected = true;

                } catch (IOException e) {
                    service.onConnectionClosed();
                    if (isToasted)
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(service, "IOException: ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    if( debug ) Log.e(TAG, "IOException: " + e.getMessage());
                } catch (SmackException e) {
                    service.onConnectionClosed();
                    if (isToasted)
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(service, "SMACKException: ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    if( debug ) Log.e(TAG, "SMACKException: " + e.getMessage());
                } catch (XMPPException e) {
                    service.onConnectionClosed();
                    if (isToasted)
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(service, "XMPPException: ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    if( debug ) Log.e(TAG, "XMPPException: " + e.getMessage());
                }

                //Our "connection" phase is now complete. We can tell others to make requests from now on.
                return isconnecting = false;
            }
        };
        connectionThread.execute();
    }

    //Explicitly Disconnect a connection
    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public String getCurrentUserDetails() {
        VCardManager vCardManager = VCardManager.getInstanceFor(connection);
        try {
            VCard mCard = vCardManager.loadVCard();
            return mCard.getTo();
        } catch ( XMPPException.XMPPErrorException e ){
            e.printStackTrace();
        } catch ( SmackException.NoResponseException e ){
            e.printStackTrace();
        } catch ( SmackException.NotConnectedException e ){
            e.printStackTrace();
        }

        return null;
    }

    //Dummy method. Checks if roster belongs to one of our user
    public Boolean checkSender(RoasterModel roasterModel, String user){
        Presence presence = roster.getPresence(user);
        return (presence.getFrom().contains(roasterModel.getRoasterPresenceFrom()));
    }

    //Sends a subscription request to particular user (JID)
    public void sendRequestTo(String jid) {

        //Making the Full JID
        if( !jid.contains("@") ){
            jid = jid + "@" + Constants.XMPP_DOMAIN;
        }

        //get Entry
        RosterEntry userEntry = roster.getEntry(jid);

        String nickname = XmppStringUtils.parseLocalpart(jid);

        boolean isSubscribed = true;
        if (userEntry != null) {
            isSubscribed = userEntry.getGroups().size() == 0;
        }

        if (isSubscribed) {
            try {
                roster.createEntry(jid, nickname, null);
            }
            catch (XMPPException | SmackException e) {
                if(debug) Log.e(TAG,"Unable to add new entry " + jid, e);
                e.printStackTrace();
            }

            roster.getEntry(jid);
        }
    }

    //Get subscription requests which came "from" other users
    public ArrayList<String> getPendingRequests() {
        Collection<RosterEntry> entries = roster.getEntries();

        ArrayList<String> pendingRequestList = new ArrayList<>();
        for(RosterEntry entry : entries) {
            Presence presence = roster.getPresence(entry.getUser());
            if(entry.getType() == RosterPacket.ItemType.from){
                pendingRequestList.add( presence.getFrom() );
            }
        }

        return pendingRequestList;
    }

    //Get only online users, i.e. users having subscription mode "BOTH" with you
    public ArrayList<RoasterModel> getOnlineUsers() {

        Collection<RosterEntry> entries = roster.getEntries();

        ArrayList<RoasterModel> roasterModelArrayList = new ArrayList<>();
        for(RosterEntry entry : entries) {
            Presence presence = roster.getPresence(entry.getUser());

            if(presence != null &&  entry.getType() == RosterPacket.ItemType.both){
                Presence.Mode mode = presence.getMode();

                int status = retreiveState(mode, presence.isAvailable());
                roasterModelArrayList.add(
                        new RoasterModel(entry.getUser(), presence.getFrom(), presence.getStatus(), mode, status));
            }

            if( debug ) {
                Log.e(TAG, ""+entry.getUser());
                Log.e(TAG, ""+entry.getName());
                Log.e(TAG, ""+presence.getType().name());
                Log.e(TAG, ""+presence.getStatus());
                Log.e(TAG, ""+presence.getMode());
                Log.e(TAG, ""+entry.getType());

                String isSubscribePending = (entry.getType() == RosterPacket.ItemType.both)?"Yes":"No";
                Log.e(TAG, "sub: "+isSubscribePending);
            }
        }

        return roasterModelArrayList;
    }

    //Getting presence mode, to check user status
    private int retreiveState(Presence.Mode usermode, boolean isOnline){

        int userState =  Constants.PRESENCE_MODE_OFFLINE_INT;

        if(usermode == Presence.Mode.dnd) {
            userState = Constants.PRESENCE_MODE_DND_INT;
        } else if (usermode == Presence.Mode.away || usermode == Presence.Mode.xa) {
            userState = Constants.PRESENCE_MODE_AWAY_INT;
        } else if( isOnline ) {
            userState = Constants.PRESENCE_MODE_AVAILABLE_INT;
        }

        return userState;

    }

    //Signup to server
    public void Signup( SignupModel signupModel) {
        XMPPError.Condition condition = null;
        boolean errors = false;
        String errorMessage = "";

        String mUsername = signupModel.getUsername();
        String mPassword = signupModel.getPassword();

        boolean isPasswordValid = signupModel.checkPassword();
        boolean areFieldsValid = signupModel.validateFields();

        if( !isPasswordValid ) {
            errors = true;
            errorMessage = Constants.SIGNUP_ERR_INVALIDPASS;
        }

        if( !areFieldsValid ) {
            errors = true;
            errorMessage = Constants.SIGNUP_ERR_FIELDERR;
        }

        if ( errors ) {
            service.onSignupFailed( errorMessage );
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if( !connected && ! isconnecting ) connect();
            }
        }).start();

        try {
            final AccountManager accountManager = AccountManager.getInstance(connection);
            accountManager.createAccount(mUsername, mPassword);
        }
        catch (XMPPException | SmackException e) {

            e.printStackTrace();
            if(debug) Log.e(TAG,"Username: "+mUsername+",Password: "+mPassword);

            if ( e instanceof XMPPException.XMPPErrorException ) {
                condition = ( (XMPPException.XMPPErrorException) e ).getXMPPError().getCondition();
            }

            if ( condition == null ) {
                condition = XMPPError.Condition.internal_server_error;
            }
        }

        if (condition == null) {
            service.onSignupSuccess();
        } else {
            switch (condition){
                case conflict:
                    errorMessage = Constants.SIGNUP_ERR_CONFLICT;
                    break;
                case internal_server_error:
                    errorMessage = Constants.SIGNUP_ERR_SERVER_ERR;
                    break;
                default:
                    errorMessage = condition.toString();
                    break;

            }

            service.onSignupFailed(errorMessage);
        }
    }

    //Login to server
    public void login() {
        try {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if( !connected && ! isconnecting ) connect();
                }
            }).start();

            if( debug ) Log.i(TAG, "User "+ userId + userPassword);

            connection.login(userId, userPassword);

            if( debug ) Log.i(TAG, "Yey! We're logged in to the Xmpp server!");

            service.onLoggedIn();
        } catch ( XMPPException | SmackException | IOException e) {

            service.onLoginFailed();
            if( debug) e.printStackTrace();
        }
    }

    //Update which chat instance is running currently. Set it to true.
    public void updateChatEntryMap(String key){
        for (Map.Entry<String, Boolean> entry : chat_created_for.entrySet()) {
            entry.setValue(entry.getKey().equals(key));
        }
    }

    // Send Message (you can call this method from other activity)
    // This method will try to create connection (if not established already),
    // will open up a TCP connection to another user (usually a roaster in jabber language),
    // will throw exception if there is an error
    public void sendMessage(ChatItem chatMessage) throws SmackException{
        String body = gson.toJson(chatMessage);

        if( chat_created_for.get(chatMessage.getReceiver()) == null )
            chat_created_for.put(chatMessage.getReceiver(),false);

        if (!chat_created_for.get(chatMessage.getReceiver())) {
            Log.e(TAG,"jusabtsend:"+chatMessage.getReceiver());
            mChat = ChatManager.getInstanceFor(connection).createChat(
                    chatMessage.getReceiver(),
                    mMessageListener);

            updateChatEntryMap(chatMessage.getReceiver());
            chat_created_for.put(chatMessage.getReceiver(),true);
        }

        final Message message = new Message();
        message.setBody(body);
        message.setStanzaId(chatMessage.getMsgId());
        message.setType(Message.Type.chat);

        try {
            if (connection.isAuthenticated()) {
                mChat.sendMessage(message);
            } else {
                login();
            }
        } catch (SmackException.NotConnectedException e) {
            if( debug ) Log.e(TAG, "msg Not sent!-Not Connected!");
            throw new SmackException(e);

        } catch (Exception e) {
            e.printStackTrace();
            if( debug ) Log.e(TAG, "msg Not sent!" + e.getMessage());
        }

    }


    // Our own connection Listener
    // Here, you can handle several connection events in our own way
    public class XMPPConnectionListener implements ConnectionListener{

        //We are connected, now we can login
        @Override
        public void connected(final XMPPConnection connection) {

            if( debug ) Log.d(TAG, "Connected!");

            service.onConnected();
            connected = true;

            if (!connection.isAuthenticated() && autoLogin) {
                login();
            }
        }

        //Our connection has closed. reset everything here and alert user.
        @Override
        public void connectionClosed() {

            service.onConnectionClosed();

            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(service, "ConnectionCLosed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });

            if( debug ) Log.d(TAG, "ConnectionCLosed!");

            connected = false;
        //    chat_created = false;
            chatInstanceIterator(chat_created_for);
            loggedin = false;
        }

        //Our connection has closed, due to error. Still, it is same thing as above. Reset everything
        @Override
        public void connectionClosedOnError(Exception arg0) {

            service.onConnectionClosed();

            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(service, "ConnectionClosedOn Error!!",
                                Toast.LENGTH_SHORT).show();

                    }
                });

            if( debug ) Log.d(TAG, "ConnectionClosedOn Error!");

            connected = false;
            chatInstanceIterator(chat_created_for);
            loggedin = false;
        }

        @Override
        public void reconnectingIn(int arg0) {

            service.onReConnection();
            if( debug ) Log.d(TAG, "Reconnectingin " + arg0);
            loggedin = false;
        }

        // Our reconnection attemp failed. Reset everything. Basically, we reset whenever our connection failed,
        // no matter whatever the cause is
        @Override
        public void reconnectionFailed(Exception arg0) {

            service.onReConnectionError();

            if( debug ) Log.d(TAG, "ReconnectionFailed!");

            //Reset the variables
            connected = false;
            chatInstanceIterator(chat_created_for);
            loggedin = false;
        }

        //Below two methods are quite useful. These handles a successfull connection attempt.
        @Override
        public void reconnectionSuccessful() {

            service.onReConnected();

            if( debug ) Log.d(TAG, "ReconnectionSuccessful");

            //We are only connected, not authenticated yet. See the next method
            connected = true;
            chatInstanceIterator(chat_created_for);
            loggedin = false;
        }

        //This is main method, we authentication stuff happens
        @Override
        public void authenticated(XMPPConnection connectionNew, boolean resumed) {

            chatInstanceIterator(chat_created_for);
            loggedin = true;

            ChatManager.getInstanceFor(connection).addChatListener(mChatManagerListener);

            //Wait for 500ms before showing we are authenticated
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

            if( debug ) Log.d(TAG,"Yay!! We are now authenticated!!");

            service.onAuthenticated();
        }
    }

    //Your own Chat Manager. We attach the message events here
    private class MyChatManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(final Chat chat, final boolean createdLocally) {
            //Attach our message listener here, so we can have better control how to parse messages.
            if (!createdLocally)
                chat.addMessageListener(mMessageListener);
        }
    }

    //Now our message Listener. We can now see how to parse XMPP messages received from other users or server.
    private class MyMessageListener implements MessageListener, ChatStateListener {

        private String TAG = getClass().getSimpleName();

        public MyMessageListener() {}

        @Override
        public void processMessage(Message message) {}

        @Override
        public void stateChanged(Chat chat, ChatState state) {

            if( debug ) Log.e(TAG,"Chat State local: "+chat.getParticipant() + ": " + state.name());

            if (state.toString().equals(ChatState.composing.toString())) {
                if( debug ) Log.e(TAG,"User is typing");
            } else if (state.toString().equals(ChatState.paused.toString())) {
                if( debug ) Log.e(TAG,"User is paused");
            } else if(state.toString().equals(ChatState.active.toString())){
                if( debug ) Log.e(TAG,"User is active");
            } else if(state.toString().equals(ChatState.gone.toString())){
                if( debug ) Log.e(TAG,"User is away");
            } else if(state.toString().equals(ChatState.inactive.toString())){
                if( debug ) Log.e(TAG,"User is inactive");
            } else{
                if( debug ) Log.e(TAG,"User is nothing");
            }

            service.onChatStateChange(new ChatStateModel(chat.getParticipant(),state));
        }

        // This is the method where we need to process the messages and parse it.
        @Override
        public void processMessage(final Chat chat, final Message message) {

            //Proceed only if we have a message and its a chat
            if (message.getType() == Message.Type.chat && message.getBody() != null) {

                if( debug ) Log.d(TAG,"New Message received: "+ message.getBody());

                //Make the newly received message as our chatitem that our listview can process
                ChatItem chatMessage = null;

                // Usually, you will code your XMPP implementation such that the sender sends a message
                // in a valid JSON representation. While this is a good practise, you may also not want to
                // miss any messages received from other sources (such as pidgin). They may not know your message
                // structure and message can be in raw string format. Thus, you can use this check if message was
                // from someone with your JSON structure, or is it from someone else. If latter is the case, you can
                // just log the message for debugging purpose, or ignore it ofcourse.

                if(Utils.isJSONValid(message.getBody()))
                    chatMessage = gson.fromJson(message.getBody(), ChatItem.class);
                else{
                    String currentDate = Utils.getCurrentDate();
                    String currentTime = Utils.getCurrentTime();

                    // Try to represent it in our message format.
                    chatMessage = new ChatItem(message.getBody(), currentDate, currentTime, message.getFrom(), getCurrentUserDetails(),false);
                }

                //Now our message is in our representation, we can send it to our list to add newly received message
                addMessage(chatMessage);
            }
        }

        private void addMessage(final ChatItem chatMessage) {
            service.onNewMessage(chatMessage);
        }
    }



    private class MyStanzaListener implements StanzaListener{
        @Override
        public void processPacket(Stanza packet) {

            //only filter Presence packets
            if( packet instanceof Presence) {
                Presence presence = (Presence) packet;
                final String fromJID = presence.getFrom();

            /* We got a request (subscription req). We need to send back "subscribe/subscribed"or "unsubscribe" based on
             * user choice. We will show user asking him to "accept" or "reject".
             */
                //@see: http://xmpp.org/rfcs/rfc6121.html#sub-request
                if (presence.getType() == Presence.Type.subscribe) {

                    if (debug) Log.e(TAG, "subscription request from - " + fromJID);
                    service.onRequestSubscribe(fromJID);

                }
            }
        }
    }

    private class MyRosterListener implements RosterListener{

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            if(debug) Log.e(TAG,"Entry deleted! ");

            Iterator addressIter = addresses.iterator();
            while (addressIter.hasNext()){
                if(debug) Log.e(TAG,"Entry deleted: "+ addressIter.next());
            }
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            if(debug) Log.e(TAG,"Entry updated! ");

            Iterator addressIter = addresses.iterator();
            while (addressIter.hasNext()){
                if(debug) Log.e(TAG,"Entry updated: "+ addressIter.next());
            }
        }

        @Override
        public void entriesAdded(Collection<String> addresses) {
            if(debug) Log.e(TAG,"Entry added! ");

            Iterator addressIter = addresses.iterator();
            while (addressIter.hasNext()){
                if(debug) Log.e(TAG,"Entry added: "+ addressIter.next());
            }
        }

        /* This is a good place to know whenever a user went online/offline. Use this method
         * to call any of your singleton, pub-subs etc to let know your UI to change user presence
         */
        @Override
        public void presenceChanged(Presence presence) {
            if(debug) Log.e(TAG,"Presence changed: " + presence.getFrom() + " " + presence.getStatus());
            Presence.Mode mode = presence.getMode();
            int status = retreiveState(mode, presence.isAvailable());
            service.onPresenceChange(new PresenceModel(presence.getFrom(),presence.getStatus(),mode,status));
        }

    }

    /*
     * Whenever a user sends a subscription request, you have to send him back two subscription requests:
     * 1. Presence.Type.subscribe
     * 2. Presence.Type.subscribed (you are now subscribed to user, at this point user can see your presence,
     *    but you can not see his presence)
     *
     */
    public void confirmSubscription(String fromJID, boolean shouldSubscribe){
        final RosterEntry newEntry = roster.getEntry(fromJID);
        //Prepare "subscribe" precense
        Presence subscribe = new Presence(Presence.Type.subscribe);
        subscribe.setTo(fromJID);

        //Prepare "subscribed" precense
        Presence subscribed = new Presence(Presence.Type.subscribed);
        subscribed.setTo(fromJID);

        //Prepare Unsubscribe
        Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
        unsubscribe.setTo(fromJID);

        //Send both (or only subscribed, if user has already sent request)
        try {
            if(shouldSubscribe) {
                if (newEntry == null || newEntry.getType() == RosterPacket.ItemType.from) {
                    connection.sendStanza(subscribed);
                    connection.sendStanza(subscribe);
                } else {
                    connection.sendStanza(subscribed);
                }
            } else {
                connection.sendStanza(unsubscribe);
            }
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }


    //Chat State events
    public void updateChatStatus(String receiver, ChatState chatState){
        if( chat_created_for.get(receiver) == null )
            chat_created_for.put(receiver,false);

        if (!chat_created_for.get(receiver)) {
            mChat = ChatManager.getInstanceFor(connection).createChat(
                    receiver,
                    mMessageListener);

            updateChatEntryMap(receiver);
            chat_created_for.put(receiver,true);
        }


        try {
            if (connection.isAuthenticated()) {
                ChatStateManager.getInstance(connection).setCurrentState(chatState, mChat);
            }
        } catch (SmackException.NotConnectedException e) {
            if( debug ) Log.e(TAG, "status Not sent!-Not Connected!");

        } catch (Exception e) {
            e.printStackTrace();
            if( debug ) Log.e(TAG, "status Not sent!" + e.getMessage());
        }
    }
}
