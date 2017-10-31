package com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.facebook.Profile;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.activities.InvitationResumerActivity;

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
        if (remoteMessage.getData().size() > 0) {
            final Map<String, String> data = remoteMessage.getData();

            if(data.size() == 2){
                mIdFacebook = data.get("idFacebook");
                mTime = Long.parseLong(data.get("time"));

                if(mIdFacebook.equals(Profile.getCurrentProfile().getId())){
                    return;
                } else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                            .getReference().child("users").child(Profile.getCurrentProfile().getId())
                            .child("scheduledEvent").child(String.valueOf(mTime)).child("users");
                    Query query = databaseReference.orderByChild("idFacebook").equalTo(mIdFacebook);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                String user = String.valueOf(snapshot.child("name").getValue());
                                canceledFriendNotification(user);
                                snapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            } else {
                mIdFacebook = data.get("idFacebook");
                mLatitudeDestination = Double.parseDouble(data.get("latitudeDestination"));
                mLongitudeDestination = Double.parseDouble(data.get("longitudeDestination"));
                mPlaceName = data.get("placeName");
                mTime = Long.parseLong(data.get("time"));
                String username = data.get("username");
                mFriendsList = data.get("friendsList");

                eventNotification(username, "Meetus");
            }
        }

        if (remoteMessage.getNotification() != null) {
            eventNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void eventNotification(String messageTitle, String messageBody) {
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
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    private void canceledFriendNotification(String name){
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(name)
                .setContentText(name + " won't participate.")
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}
