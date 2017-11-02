package com.silho.ideo.meetus.fjd;

import android.content.Context;
import android.icu.util.TimeUnit;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by Samuel on 01/11/2017.
 */

public class Ru {

    public static final int REMINDER_INTERVAL_MINUTES = 1;
    public static final int REMINDER_INTERVAL_SECONDES = (int) (java.util.concurrent.TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));
    public static final String REMINDER_JOB_TAG = "need_to_go_reminder_tag";
    private static boolean sInitialized;

    synchronized public static void scheduleReminder(@NonNull final Context context){
        if(sInitialized)return;
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
        sInitialized = true;
    }
}
