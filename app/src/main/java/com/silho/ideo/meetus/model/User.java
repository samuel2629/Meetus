package com.silho.ideo.meetus.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Samuel on 01/08/2017.
 */

public class User implements Parcelable {

    public String token, idFacebook, name, profilPic;
    public double latitude, longitude;

    public User(){}

    public User(String token, double myLatitude, double myLongitude, String idFacebook, String name, String profilPic){
        this.token = token;
        this.latitude = myLatitude;
        this.longitude = myLongitude;
        this.idFacebook = idFacebook;
        this.name = name;
        this.profilPic = profilPic;

    }

    protected User(Parcel in) {
        token = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        idFacebook = in.readString();
        name = in.readString();
        profilPic = in.readString();
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", token);
            obj.put("latitude", latitude);
            obj.put("longitude", longitude);
            obj.put("idFacebook", idFacebook);
            obj.put("name", name);
            obj.put("profilPic", profilPic);
        } catch (JSONException e) {
        }
        return obj;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(token);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(idFacebook);
        parcel.writeString(name);
        parcel.writeString(profilPic);
    }
}
