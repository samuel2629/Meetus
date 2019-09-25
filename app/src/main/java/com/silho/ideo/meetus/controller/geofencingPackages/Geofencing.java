package com.silho.ideo.meetus.controller.geofencingPackages;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * Created by Samuel on 24/07/2017.
 */

public class Geofencing implements ResultCallback{

    private PendingIntent mGeofencingPendingIntent;
    private ArrayList<Geofence> mGeofenceList;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCE_RADIUS = 50;
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000;

    public Geofencing(Context context, GoogleApiClient googleApiClient){
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mGeofencingPendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    public void registerAllGeofences() {
        // Check that the API client is connected and that the list has Geofences in it
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencingPendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }

    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in registerGeofences
                    getGeofencingPendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }

    public void updateGeofencesList(PlaceBuffer places){
        mGeofenceList = new ArrayList<>();
        if(places == null || places.getCount() == 0){
            for(Place place : places){
                String placeUID = place.getId();
                double placeLat = place.getLatLng().latitude;
                double placeLng = place.getLatLng().longitude;
                Geofence geofence = new Geofence.Builder()
                        .setRequestId(placeUID)
                        .setExpirationDuration(GEOFENCE_TIMEOUT)
                        .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build();

                mGeofenceList.add(geofence);
            }
        }
    }

    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencingPendingIntent(){
        if(mGeofencingPendingIntent != null){
            return mGeofencingPendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencingPendingIntent = PendingIntent.getBroadcast(mContext, 8, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencingPendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding/removing geofence : %s",
                result.getStatus().toString()));

    }
}
