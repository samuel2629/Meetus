package com.silho.ideo.meetus.firebaseCloudMessaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.activities.InvitationResumerActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;

/**
 * Created by Samuel on 08/08/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String ID_FACEBOOK = "facebookID";
    public static final String LATITUDE_DEST = "latitude_destination";
    public static final String LONGITUDE_DEST = "longitude_destination";
    public static final String PLACE_NAME = "place_name";
    public static final String TIME = "time";
    public static final String FRIENDS_LIST = "friends_list";
    public static final String FRIENDS_LIST_INVITED = "friends_list_invited";

    private String mIdFacebook;
    private double mLatitudeDestination;
    private double mLongitudeDestination;
    private String mPlaceName;
    private long mTime;
    private String mFriendsList;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only recei  ved here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();

            mIdFacebook = data.get("idFacebook");
            mLatitudeDestination = Double.parseDouble(data.get("latitudeDestination"));
            mLongitudeDestination = Double.parseDouble(data.get("longitudeDestination"));
            mPlaceName = data.get("placeName");
            mTime = Long.parseLong(data.get("time"));
            String username = data.get("username");
            mFriendsList = data.get("friendsList");

            sendNotification(username, "Wanna Meetus ?");

            if (true) {
                scheduleJob();
            } else {
                handleNow();
            }

        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void scheduleJob() {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendNotification(String messageTitle, String messageBody) {
        Intent intent = new Intent(this, InvitationResumerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ID_FACEBOOK, mIdFacebook);
        bundle.putDouble(LATITUDE_DEST, mLatitudeDestination);
        bundle.putDouble(LONGITUDE_DEST, mLongitudeDestination);
        bundle.putString(PLACE_NAME, mPlaceName);
        bundle.putLong(TIME, mTime);
        bundle.putString(FRIENDS_LIST, mFriendsList);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_free_breakfast_black_24dp)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
