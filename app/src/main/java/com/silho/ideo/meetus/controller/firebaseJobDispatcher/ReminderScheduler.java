package com.silho.ideo.meetus.controller.firebaseJobDispatcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.silho.ideo.meetus.controller.detectionActivityPackages.DetectedActivitiesIntentService;
import com.silho.ideo.meetus.controller.detectionActivityPackages.DetectionActivity;

/**
 * Created by Samuel on 01/11/2017.
 */

public class ReminderScheduler {

    public static final int REMINDER_INTERVAL_MINUTES = 1;
    public static final int REMINDER_INTERVAL_SECONDES = (int) (java.util.concurrent.TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));
    public static final String REMINDER_JOB_TAG = "need_to_go_reminder_tag";
    public static boolean sInitialized;

    synchronized public static void scheduleReminder(@NonNull final Context context){

        /*if(sInitialized)return;
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(NeedToGoReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        REMINDER_INTERVAL_SECONDES,
                        REMINDER_INTERVAL_SECONDES + REMINDER_INTERVAL_SECONDES))
                .setReplaceCurrent(true)
                .build();
        dispatcher.schedule(constraintReminderJob);
        sInitialized = true;*/

        if(sInitialized)return;
        Intent intent = new Intent(context, DetectionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30000L, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 60* 1000, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
        Log.e(REMINDER_JOB_TAG, "ALARM MANAGER SCHEDULED");
        sInitialized = true;
    }
}
