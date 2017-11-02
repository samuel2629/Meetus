package com.silho.ideo.meetus.controller.detectionActivityPackages;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.silho.ideo.meetus.R;

import java.util.ArrayList;

/**
 * Created by Samuel on 27/07/2017.
 */

public class DetectedActivitiesIntentService extends IntentService{
    public static final String TAG = "detection_is";

    public DetectedActivitiesIntentService(){
        super(TAG);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onHandleIntent(@Nullable Intent intent) {
        ActivityRecognitionResult recognitionResult = ActivityRecognitionResult.extractResult(intent);
        //Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) recognitionResult.getProbableActivities();
        Log.i(TAG, "activities detected");

        String strStatus = "";
        for(DetectedActivity thisActivity: detectedActivities){
            strStatus += getActivityString(thisActivity.getType())+ thisActivity.getConfidence() + "%\n";
        }
        Log.i(TAG, strStatus);
        /*localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);*/
    }

    public String getActivityString(int detectedActivityType) {
        Resources resources = this.getResources();
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            default:
                return resources.getString(R.string.unidentifiable_activity);
        }
    }
}
