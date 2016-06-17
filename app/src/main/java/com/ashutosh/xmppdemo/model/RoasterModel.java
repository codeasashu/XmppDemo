package com.ashutosh.xmppdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jivesoftware.smack.packet.Presence;



public class RoasterModel implements Parcelable {

    public String roasterEntryUser;
    public String roasterPresenceFrom;
    public String presenceStatus;
    public Presence.Mode presenceMode;
    public int status;

    public RoasterModel(String roasterEntryUser, String roasterPresenceFrom, String presenceStatus, Presence.Mode presenceMode, int status){
        this.roasterEntryUser = roasterEntryUser;
        this.roasterPresenceFrom = roasterPresenceFrom;
        this.presenceStatus = presenceStatus;
        this.presenceMode = presenceMode;
        this.status = status;
    }

    public String getRoasterEntryUser(){
        return this.roasterEntryUser;
    }

    public String getRoasterPresenceFrom(){
        return this.roasterPresenceFrom;
    }

    public String getPresenceStatus() {
        return this.presenceStatus;
    }

    public Presence.Mode getPresenceMode() {
        return this.presenceMode;
    }

    public void setPresenceMode(Presence.Mode mode) {
        this.presenceMode = mode;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    protected RoasterModel(Parcel in) {
        roasterEntryUser = in.readString();
        roasterPresenceFrom = in.readString();
        presenceStatus = in.readString();
        presenceMode = (Presence.Mode) in.readValue(Presence.Mode.class.getClassLoader());
        status = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roasterEntryUser);
        dest.writeString(roasterPresenceFrom);
        dest.writeString(presenceStatus);
        dest.writeValue(presenceMode);
        dest.writeInt(status);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RoasterModel> CREATOR = new Parcelable.Creator<RoasterModel>() {
        @Override
        public RoasterModel createFromParcel(Parcel in) {
            return new RoasterModel(in);
        }

        @Override
        public RoasterModel[] newArray(int size) {
            return new RoasterModel[size];
        }
    };
}