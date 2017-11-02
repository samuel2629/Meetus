package com.silho.ideo.meetus.controller.detectionActivityPackages;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.silho.ideo.meetus.R;

import java.util.ArrayList;

/**
 * Created by Samuel on 01/08/2017.
 */

public class DetectionActivity implements ResultCallback<Status>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = DetectionActivity.class.getSimpleName();
    private Context mContext;
    //private ActivityDetectionBroadcastReceiver mActivityDetectionBroadcastReceiver;
    private GoogleApiClient mClient;


    public DetectionActivity(Context context){
        mContext = context;
        mClient = buildGoogleApiClient(mContext);
        //mActivityDetectionBroadcastReceiver = new ActivityDetectionBroadcastReceiver(mContext);
    }

    public GoogleApiClient buildGoogleApiClient(Context context) {
         return mClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    public GoogleApiClient getClient() {
        return mClient;
    }

//    public void registerBroadcastReceiver(){
//        LocalBroadcastManager.getInstance(mContext).registerReceiver(mActivityDetectionBroadcastReceiver, new IntentFilter(Constants.BROADCAST_ACTION));
//    }
//
//    public void unregisterBroadcastReceiver(){
//        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mActivityDetectionBroadcastReceiver);
//    }

//    public void requestActivityUpdates() {
//        if (!mClient.isConnected()) {
//            Toast.makeText(mContext, "not connected", Toast.LENGTH_SHORT).show();
//            return;
//        } ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
//                mClient,
//                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
//                getActivityDetectionPendingIntent()
//        ).setResultCallback(this);
//    }
//
//    public void removeActivityUpdates() {
//        if (!mClient.isConnected()) {
//            Toast.makeText(mContext, "not connected", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
//                mClient,
//                getActivityDetectionPendingIntent()
//        ).setResultCallback(this);
//    }

//    private PendingIntent getActivityDetectionPendingIntent() {
//        Intent intent = new Intent(mContext, DetectedActivitiesIntentService.class);
//        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.e(TAG, "Successfully added activity detection.");

        } else {
            Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(mContext, DetectedActivitiesIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mClient, Constants.DETECTION_INTERVAL_IN_MILLISECONDS, pendingIntent);
        Log.i(TAG, "Connected");
    }

    public void removeUpdates(){
        Intent intent = new Intent(mContext, DetectedActivitiesIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mClient, pendingIntent);
        mClient.disconnect();
        Log.i(TAG, "disconnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

   /* public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

        public static final String TAG = "receiver";
        private Context mContext;

        public ActivityDetectionBroadcastReceiver(Context context){
            mContext = context;
        }

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
    }*/
}
