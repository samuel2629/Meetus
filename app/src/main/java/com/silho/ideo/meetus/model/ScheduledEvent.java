package com.silho.ideo.meetus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Samuel on 01/08/2017.
 */

public class ScheduledEvent implements Parcelable{

    private LatLng mLatLng;
    private Date mDate;

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }


    protected ScheduledEvent(Parcel in) {
        mLatLng = in.readParcelable(LatLng.class.getClassLoader());
        mDate = in.readParcelable(Date.class.getClassLoader());
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
        parcel.writeParcelable(mLatLng, i);
        parcel.writeParcelable((Parcelable) mDate, i);
    }
}
