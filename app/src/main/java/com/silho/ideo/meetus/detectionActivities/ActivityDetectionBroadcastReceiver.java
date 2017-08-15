package com.silho.ideo.meetus.detectionActivities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.silho.ideo.meetus.R;

import java.util.ArrayList;

/**
 * Created by Samuel on 27/07/2017.
 */

public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "receiver";
    private Context mContext;


    public ActivityDetectionBroadcastReceiver(Context context){
        mContext = context;
    }

    public ActivityDetectionBroadcastReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<DetectedActivity> updatedActivities =
                intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
        String strStatus = "";
        for(DetectedActivity thisActivity: updatedActivities){
            strStatus += getActivityString(thisActivity.getType())+ thisActivity.getConfidence() + "%\n";
        }
        Log.i(TAG, strStatus);
    }

    public String getActivityString(int detectedActivityType){
        Resources resources = mContext.getResources();
        switch (detectedActivityType){
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
            default: return resources.getString(R.string.unidentifiable_activity);
        }
    }
}
