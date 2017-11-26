package com.silho.ideo.meetus.controller.recognitionActivityPackages;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.silho.ideo.meetus.controller.alarmManager.ReminderScheduler;

/**
 * Created by Samuel on 01/08/2017.
 */

public class DetectionActivityIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = DetectionActivityIntentService.class.getSimpleName();
    private Context mContext;
    private static GoogleApiClient mClient;

    public DetectionActivityIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mContext = this;
        buildGoogleApiClient(this);
        onConnected(null);
    }

    private void buildGoogleApiClient(Context context) {
        mClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    public static GoogleApiClient getClient() {
        return mClient;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(mContext, DetectedActivitiesIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.getClient(mContext).requestActivityUpdates(
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS, pendingIntent
        );
        Log.i(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static void removeUpdates(Context context){
        Intent intent = new Intent(context, DetectedActivitiesIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.getClient(context).removeActivityUpdates(pendingIntent);
        ReminderScheduler.sInitialized = false;
        ReminderScheduler.scheduleReminder(context);
        Log.e(TAG, "is removed");
    }
}
