package com.ashutosh.xmppdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

public class ChatItem implements Parcelable {

    private String chat;
    private String date;
    private String time;
    private String sender;
    private String receiver;
    private boolean isMine;

    public ChatItem(String chat, String date, String time, String sender, String receiver, boolean isMine){
        this.chat = chat;
        this.date = date;
        this.isMine = isMine;
        this.time = time;
        this.sender = sender;
        this.receiver = receiver;
    }

    public void setIsMine(Boolean isMine){
        this.isMine = isMine;
    }

    public String getMsgId(){
        return String.format("%02d", new Random().nextInt(100));
    }

    public String getDate(){
        return this.date;
    }

    public String getChat(){
        return this.chat;
    }

    public String getTime(){
        return this.time;
    }

    public String getSender(){
        return this.sender;
    }
    public String getReceiver(){
        return this.receiver;
    }

    public boolean isMine(){
        return this.isMine;
    }

    //Parcellable Content
    protected ChatItem(Parcel in) {
        chat = in.readString();
        date = in.readString();
        time = in.readString();
        sender = in.readString();
        receiver = in.readString();
        isMine = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chat);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(sender);
        dest.writeString(receiver);
        dest.writeByte((byte) (isMine ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ChatItem> CREATOR = new Parcelable.Creator<ChatItem>() {
        @Override
        public ChatItem createFromParcel(Parcel in) {
            return new ChatItem(in);
        }

        @Override
        public ChatItem[] newArray(int size) {
            return new ChatItem[size];
        }
    };
}
