package com.ashutosh.xmppdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jivesoftware.smack.packet.Presence;

public class PresenceModel implements Parcelable {

    String user;
    String status;
    Presence.Mode mode;
    int userStatus;

    public PresenceModel(){}

    public PresenceModel(String user, String status, Presence.Mode mode, int userStatus){
        this.user = user;
        this.status = status;
        this.mode = mode;
        this.userStatus = userStatus;
    }

    public String getUser(){
        return this.user;
    }

    public String getStatus(){
        return this.status;
    }

    public Presence.Mode getMode(){
        return this.mode;
    }

    public int getUserStatus(){
        return this.userStatus;
    }

    protected PresenceModel(Parcel in) {
        user = in.readString();
        status = in.readString();
        mode = (Presence.Mode) in.readValue(Presence.Mode.class.getClassLoader());
        userStatus = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
        dest.writeString(status);
        dest.writeValue(mode);
        dest.writeInt(userStatus);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PresenceModel> CREATOR = new Parcelable.Creator<PresenceModel>() {
        @Override
        public PresenceModel createFromParcel(Parcel in) {
            return new PresenceModel(in);
        }

        @Override
        public PresenceModel[] newArray(int size) {
            return new PresenceModel[size];
        }
    };
}
