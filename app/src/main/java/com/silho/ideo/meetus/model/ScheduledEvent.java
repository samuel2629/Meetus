package com.silho.ideo.meetus.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Samuel on 01/08/2017.
 */

public class ScheduledEvent implements Parcelable{

    private double latitude, longitude;
    private String placeName;
    private long timestamp;
    private boolean isScheduled;
    private ArrayList<User> users;

    public ScheduledEvent(){}

    public ScheduledEvent(long timestamp, String placeName, double latitude,
                          double longitude, boolean isScheduled, ArrayList<User> users){
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.placeName = placeName;
        this.isScheduled = isScheduled;
        this.users = users;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public boolean getIsScheduled(){
        return isScheduled;
    }

    public void setIsScheduled(boolean isScheduled){
        this.isScheduled = isScheduled;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public ScheduledEvent(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        placeName = in.readString();
        timestamp = in.readLong();
        isScheduled = in.readByte() != 0;
        in.readTypedList(users, User.CREATOR);
    }

    public static final Creator<ScheduledEvent> CREATOR = new Creator<ScheduledEvent>() {
        @Override
        public ScheduledEvent createFromParcel(Parcel in) {
            return new ScheduledEvent(in);
        }

        @Override
        public ScheduledEvent[] newArray(int size) {
            return new ScheduledEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(placeName);
        parcel.writeLong(timestamp);
        parcel.writeByte((byte) (isScheduled ? 1 : 0));
        parcel.writeTypedList(users);
    }
}
