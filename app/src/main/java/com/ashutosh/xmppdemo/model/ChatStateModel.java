package com.ashutosh.xmppdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jivesoftware.smackx.chatstates.ChatState;

public class ChatStateModel implements Parcelable {
    String user;
    ChatState chatState;

    public ChatStateModel(){}

    public ChatStateModel(String user, ChatState chatState) {
        this.user = user;
        this.chatState = chatState;
    }

    public String getUser(){
        return this.user;
    }

    public ChatState getChatState(){
        return this.chatState;
    }

    protected ChatStateModel(Parcel in) {
        user = in.readString();
        chatState = (ChatState) in.readValue(ChatState.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
        dest.writeValue(chatState);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ChatStateModel> CREATOR = new Parcelable.Creator<ChatStateModel>() {
        @Override
        public ChatStateModel createFromParcel(Parcel in) {
            return new ChatStateModel(in);
        }

        @Override
        public ChatStateModel[] newArray(int size) {
            return new ChatStateModel[size];
        }
    };
}
