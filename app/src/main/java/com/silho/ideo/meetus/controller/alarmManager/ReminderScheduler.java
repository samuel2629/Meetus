package com.silho.ideo.meetus.controller.alarmManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.silho.ideo.meetus.controller.recognitionActivityPackages.DetectedActivitiesIntentService;
import com.silho.ideo.meetus.controller.recognitionActivityPackages.DetectionActivity;
import com.silho.ideo.meetus.model.ScheduledEvent;

/**
 * Created by Samuel on 01/11/2017.
 */

public class ReminderScheduler {
    public static final String REMINDER_JOB_TAG = "need_to_go_reminder_tag";
    public static final String IS_TRANSPORT_TYPE_SET = "is_transport_type_set";
    public static boolean sInitialized;
    private static int mTransportType;

    synchronized public static void scheduleReminder(@NonNull final Context context) {
        if (sInitialized) return;

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
                                        mTransportType = se.getTransportType();
                                        if(mTransportType == 0) setAlarmAndActivityRecognition(context);
                                        else setAlarm(context, mTransportType);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            mTransportType = se.getTransportType();
                            if(mTransportType == 0) setAlarmAndActivityRecognition(context);
                            else setAlarm(context, mTransportType);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void setAlarm(Context context, int transportType) {
        Intent intent = new Intent(context, DetectedActivitiesIntentService.class);
        intent.putExtra(IS_TRANSPORT_TYPE_SET, transportType);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
        Log.e(REMINDER_JOB_TAG, "ALARM MANAGER SCHEDULED");
        sInitialized = true;

    }

    private static void setAlarmAndActivityRecognition(@NonNull Context context) {
        Intent intent = new Intent(context, DetectionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
        Log.e(REMINDER_JOB_TAG, "ALARM MANAGER SCHEDULED");
        sInitialized = true;
    }
}