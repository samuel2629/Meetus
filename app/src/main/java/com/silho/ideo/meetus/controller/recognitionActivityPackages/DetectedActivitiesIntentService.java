package com.silho.ideo.meetus.controller.recognitionActivityPackages;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.facebook.Profile;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.activities.EventResumerActivity;
import com.silho.ideo.meetus.alarmManager.ReminderScheduler;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.parsersAndCreators.TrajectCreator;

import java.util.ArrayList;

import static com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService.FRIENDS_LIST;
import static com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService.LATITUDE_DEST;
import static com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService.LONGITUDE_DEST;
import static com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService.PLACE_NAME;
import static com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService.TIME;
import static com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService.TRANSPORT_TYPE;

/**
 * Created by Samuel on 27/07/2017.
 */

public class DetectedActivitiesIntentService extends IntentService implements TrajectCreator.AsyncResponse,
TrajectCreator.AsyncResponseDuration {

    public static final int NOTIFICATION_ID = 137;
    public static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    private double mMyLatitude;
    private double mMyLongitude;
    private long mTimeNextRdv;
    private double mLatitudeDestination;
    private double mLongitudeDestination;
    private String mPlaceName;
    private String mFriendsList;
    private String mTransportTypeText;

    private TrajectCreator mTrajectCreator;
    private int mTransportType;


    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onHandleIntent(@Nullable final Intent intent) {
        Log.e(TAG, "Service Starts Is Ok !");
        if(mTrajectCreator == null) {
            mTrajectCreator = new TrajectCreator(DetectedActivitiesIntentService.this, null);
        }

        if(intent != null && intent.getExtras() != null && intent.getExtras().getInt(ReminderScheduler.IS_TRANSPORT_TYPE_SET) != 0){
            mTransportType = intent.getExtras().getInt(ReminderScheduler.IS_TRANSPORT_TYPE_SET);
            switch (mTransportType){
                case 1:
                    mTransportTypeText = "bicycling";
                    break;
                case 2:
                    mTransportTypeText = "transit";
                    break;
                case 3:
                    mTransportTypeText = "driving";
                    break;
                case 4:
                    mTransportTypeText = "walking";
                    break;
            }
            getLocation();
            requestNextEvent(mTransportTypeText);
        } else {
            String activityRecognized = getActivityRecognition(intent);
            getLocation();
            requestNextEvent(activityRecognized);
        }
    }

    private void requestNextEvent(final String activityRecognized) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users")
                .child(Profile.getCurrentProfile().getId()).child("scheduledEvent");
        final Query query = database.orderByKey().limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren() || dataSnapshot.exists()) {
                    if (dataSnapshot.getChildren().iterator().next().getValue(ScheduledEvent.class) != null) {
                        ScheduledEvent se = dataSnapshot.getChildren().iterator().next().getValue(ScheduledEvent.class);

                        if (System.currentTimeMillis() / 1000 > se.getTimestamp()) {
                            dataSnapshot.getChildren().iterator().next().getRef().removeValue();
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getChildren().iterator().next().getValue(ScheduledEvent.class) != null) {
                                        ScheduledEvent se = dataSnapshot.getChildren().iterator().next().getValue(ScheduledEvent.class);
                                        mTimeNextRdv = se.getTimestamp();
                                        mPlaceName = se.getPlaceName();
                                        mLatitudeDestination = se.getLatitude();
                                        mLongitudeDestination = se.getLongitude();
                                        if (se.getUsers() != null) {
                                            mFriendsList = se.getUsers().toString();
                                        }
                                        StringBuilder stringBuilder = mTrajectCreator.stringBuilderPlaceDestination(
                                                mMyLatitude,
                                                mMyLongitude,
                                                mLatitudeDestination,
                                                mLongitudeDestination,
                                                activityRecognized
                                        );
                                        new TrajectCreator.PlacesTask(DetectedActivitiesIntentService.this).execute(stringBuilder.toString());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            mTimeNextRdv = se.getTimestamp();
                            mPlaceName = se.getPlaceName();
                            mLatitudeDestination = se.getLatitude();
                            mLongitudeDestination = se.getLongitude();
                            if (se.getUsers() != null) {
                                mFriendsList = se.getUsers().toString();
                            }
                            StringBuilder stringBuilder = mTrajectCreator.stringBuilderPlaceDestination(
                                    mMyLatitude,
                                    mMyLongitude,
                                    mLatitudeDestination,
                                    mLongitudeDestination,
                                    activityRecognized
                            );
                            new TrajectCreator.PlacesTask(DetectedActivitiesIntentService.this).execute(stringBuilder.toString());
                        }
                    }
                } else {
                    DetectionActivityIntentService.removeUpdates(DetectedActivitiesIntentService.this);
                    stopSelf();
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getActivityRecognition(@Nullable Intent intent) {
        ActivityRecognitionResult recognitionResult = ActivityRecognitionResult.extractResult(intent);
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) recognitionResult.getProbableActivities();
        return getActivityString(detectedActivities.get(0).getType());
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(location -> {
            mMyLatitude = location.getLatitude();
            mMyLongitude = location.getLongitude();
        });
    }

    public String getActivityString(int detectedActivityType) {
        Resources resources = this.getResources();
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                mTransportType = 3;
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                mTransportType = 1;
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                mTransportType = 4;
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                mTransportType = 4;
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                mTransportType = 4;
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                mTransportType = 4;
                return resources.getString(R.string.tilting);
            case DetectedActivity.WALKING:
                mTransportType = 4;
                return resources.getString(R.string.walking);
            case DetectedActivity.UNKNOWN:
                mTransportType = 4;
                return resources.getString(R.string.unknown);
            default:
                return resources.getString(R.string.unidentifiable_activity);
        }
    }

    @Override
    public void parsingTask(String output) {
        new TrajectCreator.ParserTask(DetectedActivitiesIntentService.this).execute(output);
    }

    @Override
    public void taskParsedAndOutput(String duration) {
        if (duration != null) {
            Log.e(TAG, "duration is not null");
            long timeAsked = System.currentTimeMillis() / 1000 + Long.parseLong(duration);
            String message = "You should get prepared for your next appointment to " + mPlaceName;
            String title = "Meetus Reminder";
            if (mTimeNextRdv != 0.0) {
                if (timeAsked + 2100 >= mTimeNextRdv && !(timeAsked >= mTimeNextRdv)) {
                    Log.e(TAG, "CONDITIONS ARE MET ");
                    Intent intent = new Intent(this, EventResumerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putDouble(LATITUDE_DEST, mLatitudeDestination);
                    bundle.putDouble(LONGITUDE_DEST, mLongitudeDestination);
                    bundle.putString(PLACE_NAME, mPlaceName);
                    bundle.putLong(TIME, mTimeNextRdv);
                    bundle.putInt(TRANSPORT_TYPE, mTransportType);
                    if (mFriendsList != null) bundle.putString(FRIENDS_LIST, mFriendsList);
                    intent.putExtras(bundle);

                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+mMyLatitude+","+mMyLongitude+"&daddr="+mLatitudeDestination+","+mLongitudeDestination;
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                    PendingIntent pendingIntentGo = PendingIntent.getActivity(this, 0, Intent.createChooser(i, "Select an application"), PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .addAction(R.drawable.ic_navigation_black_24dp, "Let's Go", pendingIntentGo)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(message));

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                    DetectionActivityIntentService.removeUpdates(this);
                } else {
                    if (DetectionActivityIntentService.getClient() != null) {
                        DetectionActivityIntentService.getClient().disconnect();
                        Log.e(TAG, "Client disconnected");
                    }
                    DetectionActivityIntentService.removeUpdates(this);
                }
            }
        } else {
            DetectionActivityIntentService.removeUpdates(this);
        }
    }
}