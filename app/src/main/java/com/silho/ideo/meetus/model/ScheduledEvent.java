package com.silho.ideo.meetus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Samuel on 01/08/2017.
 */

public class ScheduledEvent implements Parcelable{

    private Double mLat, mLong;
    private String mPlaceName;
    private long mTp;
    private boolean mIsScheduled;
    private ArrayList<User> mUsers;


    public ScheduledEvent(){}

    public ScheduledEvent(long tp, String placeName, double latitudeDestination,
                          double longitudeDestination, boolean isScheduled, ArrayList<User> users){
        mLat = latitudeDestination;
        mLong = longitudeDestination;
        mTp = tp;
        mPlaceName = placeName;
        mIsScheduled = isScheduled;
        mUsers = users;
    }

    public void setLat(Double lat) {
        mLat = lat;
    }

    public void setLong(Double aLong) {
        mLong = aLong;
    }

    public void setPlaceName(String placeName) {
        mPlaceName = placeName;
    }

    public void setTp(long tp) {
        mTp = tp;
    }

    public void setScheduled(boolean scheduled) {
        mIsScheduled = scheduled;
    }

    public void setUsers(ArrayList<User> users) {
        mUsers = users;
    }

    public boolean isScheduled() {
        return mIsScheduled;
    }

    public ArrayList<User> getUsers() {
        return mUsers;
    }

    public long getTp() {
        return mTp;
    }

    public Double getLat() {
        return mLat;
    }

    public Double getLong() {
        return mLong;
    }

    public String getPlaceName() {
        return mPlaceName;
    }



    public ScheduledEvent(Parcel in) {
        mLat = in.readDouble();
        mLong = in.readDouble();
        mPlaceName = in.readString();
        mTp = in.readLong();
        mIsScheduled = in.readByte() != 0;
        in.readTypedList(mUsers, User.CREATOR);
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
        parcel.writeDouble(mLat);
        parcel.writeDouble(mLong);
        parcel.writeString(mPlaceName);
        parcel.writeLong(mTp);
        parcel.writeByte((byte) (mIsScheduled ? 1 : 0));
        parcel.writeTypedList(mUsers);
    }
}
