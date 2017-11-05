package com.silho.ideo.meetus.controller.firebaseJobDispatcher;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.activities.MainActivity;
import com.silho.ideo.meetus.controller.detectionActivityPackages.DetectionActivity;

import java.util.Random;

/**
 * Created by Samuel on 01/11/2017.
 */

public class NeedToGoReminderFirebaseJobService extends JobService {
    private static final String TAG = NeedToGoReminderFirebaseJobService.class.getSimpleName();
    private AsyncTask mBackgoundTask;
    DetectionActivity mDetectionActivity;


    @Override
    @SuppressWarnings("unchecked")
    public boolean onStartJob(final JobParameters job) {
        mDetectionActivity = new DetectionActivity(NeedToGoReminderFirebaseJobService.this);
        mDetectionActivity.getClient().connect();
        jobFinished(job, false);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if(mBackgoundTask != null) mBackgoundTask.cancel(true);
        return true;
    }

    private void notif(Context context){
        PendingIntent pi = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle("Android Job Demo")
                .setContentText("Notification from Android Job Demo App.")
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_free_breakfast_black_24dp)
                .setShowWhen(true)
                .setColor(Color.RED)
                .setLocalOnly(true)
                .build();

        NotificationManagerCompat.from(context)
                .notify(new Random().nextInt(), notification);
    }
}
