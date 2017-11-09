package com.silho.ideo.meetus.controller.detectionActivityPackages;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.activities.MainActivity;
import com.silho.ideo.meetus.controller.firebaseJobDispatcher.ReminderScheduler;

import java.util.ArrayList;

/**
 * Created by Samuel on 01/08/2017.
 */

public class DetectionActivity extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = DetectionActivity.class.getSimpleName();
    private Context mContext;
    private static GoogleApiClient mClient;

    public DetectionActivity() {
        super(TAG);
    }

    private GoogleApiClient buildGoogleApiClient(Context context) {
         return mClient = new GoogleApiClient.Builder(context)
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

    public static void removeUpdates(Context context, GoogleApiClient googleApiClient){
        Intent intent = new Intent(context, DetectedActivitiesIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.getClient(context).removeActivityUpdates(pendingIntent);
        if(googleApiClient.isConnected()) {
            googleApiClient.disconnect();
            Log.i(TAG, "disconnected");
        }
        ReminderScheduler.sInitialized = false;
        ReminderScheduler.scheduleReminder(context);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mContext = this;
        buildGoogleApiClient(this);
        onConnected(null);
    }
}
