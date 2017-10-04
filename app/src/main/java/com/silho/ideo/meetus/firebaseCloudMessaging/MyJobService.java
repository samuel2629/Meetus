package com.silho.ideo.meetus.firebaseCloudMessaging;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Samuel on 08/08/2017.
 */

public class MyJobService implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag){
            case DemoSyncJob.TAG:
                return new DemoSyncJob();
            default:
                return null;
        }
    }
}