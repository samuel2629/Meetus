package com.silho.ideo.meetus.controller.detectionActivityPackages;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.facebook.Profile;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.parsers.TrajectCreator;

import java.util.ArrayList;

/**
 * Created by Samuel on 27/07/2017.
 */

public class DetectedActivitiesIntentService extends IntentService implements TrajectCreator.AsyncResponse,
TrajectCreator.AsyncResponseDuration{
    public static final String TAG = "detection_is";
    private double mMyLatitude;
    private double mMyLongitude;
    private String mActivityRecognized;
    private TrajectCreator mTrajectCreator;
    private long mTimeNextRdv;
    private String mPlaceName;
    public static final int  NOTIFICATION_ID = 137;

    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    @SuppressLint("MissingPermission")
    @Override
    @SuppressWarnings("unchecked")
    protected void onHandleIntent(@Nullable Intent intent) {
        mTrajectCreator = new TrajectCreator(DetectedActivitiesIntentService.this, null);

        ActivityRecognitionResult recognitionResult = ActivityRecognitionResult.extractResult(intent);
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) recognitionResult.getProbableActivities();
        mActivityRecognized = getActivityString(detectedActivities.get(0).getType());

        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mMyLatitude = location.getLatitude();
                mMyLongitude = location.getLongitude();
            }
        });

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users")
                .child(Profile.getCurrentProfile().getId()).child("scheduledEvent");
        Query query = database.orderByKey().limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ScheduledEvent se = dataSnapshot.getChildren().iterator().next().getValue(ScheduledEvent.class);
                mTimeNextRdv = se.getTimestamp();
                mPlaceName = se.getPlaceName();
                StringBuilder stringBuilder = mTrajectCreator.stringBuilderPlaceDestination(
                        mMyLatitude,
                        mMyLongitude,
                        se.getLatitude(),
                        se.getLongitude(),
                        mActivityRecognized
                );
                new TrajectCreator.PlacesTask(DetectedActivitiesIntentService.this).execute(stringBuilder.toString());
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

    @Override
    public void processAlmostFinish(String output) {
        new TrajectCreator.ParserTask(DetectedActivitiesIntentService.this).execute(output);
    }

    @Override
    public void processFinish(String duration) {
        long currentTime = System.currentTimeMillis();
        String message = "You should get prepared for your next appointment to " + mPlaceName;
        String title = "Meetus Reminder";
        if(mTimeNextRdv != 0.0) {
            if (((currentTime / 1000 + Long.parseLong(duration)) + 600) >= (mTimeNextRdv - 300)) {
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                DetectionActivity detectionActivity = new DetectionActivity(this);
                detectionActivity.removeUpdates();
            }
            else {
                DetectionActivity detectionActivity = new DetectionActivity(this);
                detectionActivity.removeUpdates();
            }
        }
    }
}
