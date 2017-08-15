package com.silho.ideo.meetus.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Samuel on 25/07/2017.
 */

public class PlaceNearby implements Parcelable{

    private String mNamePlaceNearby;
    private String mVincinityPlaceNearby;
    private String mPhotoRefPlaceNearby;
    private double mLatitude;
    private double mLongitude;

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }


    public String getPhotoRefPlaceNearby() {
        return mPhotoRefPlaceNearby;
    }

    public void setPhotoRefPlaceNearby(String photoRefPlaceNearby) {
        mPhotoRefPlaceNearby = photoRefPlaceNearby;
    }

    public String getNamePlaceNearby() {
        return mNamePlaceNearby;
    }

    public void setNamePlaceNearby(String name) {
        mNamePlaceNearby = name;
    }

    public String getVincinityPlaceNearby() {
        return mVincinityPlaceNearby;
    }

    public void setVincinityPlaceNearby(String vincinityPlaceNearby) {
        mVincinityPlaceNearby = vincinityPlaceNearby;
    }


    public PlaceNearby(Parcel in) {
        mNamePlaceNearby = in.readString();
        mVincinityPlaceNearby = in.readString();
        mPhotoRefPlaceNearby = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
    }

    public PlaceNearby(){}

    public static final Creator<PlaceNearby> CREATOR = new Creator<PlaceNearby>() {
        @Override
        public PlaceNearby createFromParcel(Parcel in) {
            return new PlaceNearby(in);
        }

        @Override
        public PlaceNearby[] newArray(int size) {
            return new PlaceNearby[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mNamePlaceNearby);
        parcel.writeString(mVincinityPlaceNearby);
        parcel.writeString(mPhotoRefPlaceNearby);
        parcel.writeDouble(mLatitude);
        parcel.writeDouble(mLongitude);
    }
}
