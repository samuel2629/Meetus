package com.silho.ideo.meetus.controller.detectionActivityPackages;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.Profile;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.model.User;

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
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) recognitionResult.getProbableActivities();
        getActivityString(detectedActivities.get(0).getType());

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users")
                .child(Profile.getCurrentProfile().getId()).child("scheduledEvent");
        Query query = database.orderByKey().limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ScheduledEvent se = dataSnapshot.getChildren().iterator().next().getValue(ScheduledEvent.class);
                Log.i(TAG, "" + se.getTimestamp());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
