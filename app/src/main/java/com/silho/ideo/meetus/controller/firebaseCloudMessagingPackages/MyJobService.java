package com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

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