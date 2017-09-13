package com.silho.ideo.meetus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Samuel on 01/08/2017.
 */

public class ScheduledEvent implements Parcelable{

    private Double mLat, mLong;
    private String mPlaceName;
    private long mTp;

    public ScheduledEvent(){}

    public ScheduledEvent(long tp, String placeName, double latitudeDestination, double longitudeDestination){
        mLat = latitudeDestination;
        mLong = longitudeDestination;
        mTp = tp;
        mPlaceName = placeName;
    }

    public long getTp() {
        return mTp;
    }

    public void setTp(long tp) {
        mTp = tp;
    }

    public Double getLat() {
        return mLat;
    }

    public void setLat(Double lat) {
        mLat = lat;
    }

    public Double getLong() {
        return mLong;
    }

    public void setLong(Double aLong) {
        mLong = aLong;
    }

    public String getPlaceName() {
        return mPlaceName;
    }

    public void setPlaceName(String placeName) {
        mPlaceName = placeName;
    }



    public ScheduledEvent(Parcel in) {
        mLat = in.readDouble();
        mLong = in.readDouble();
        mPlaceName = in.readString();
        mTp = in.readLong();
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
    }
}
