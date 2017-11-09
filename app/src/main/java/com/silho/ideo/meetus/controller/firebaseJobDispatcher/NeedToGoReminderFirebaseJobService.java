package com.silho.ideo.meetus.controller.firebaseJobDispatcher;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.silho.ideo.meetus.controller.detectionActivityPackages.DetectionActivity;

/**
 * Created by Samuel on 01/11/2017.
 */

public class NeedToGoReminderFirebaseJobService extends JobService {
    private static final String TAG = NeedToGoReminderFirebaseJobService.class.getSimpleName();
    private JobTask mBackgoundTask;

    @Override
    @SuppressWarnings("unchecked")
    public boolean onStartJob(final JobParameters job) {
        Log.e(TAG, "START");
        mBackgoundTask = new JobTask(this, this);
        mBackgoundTask.execute(job);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if(mBackgoundTask != null) mBackgoundTask.cancel(true);
        return true;
    }

    private static class JobTask extends AsyncTask<JobParameters, Void, JobParameters>{

        private final JobService jobservice;
        private final Context context;
        DetectionActivity mDetectionActivity;

        public JobTask(JobService jobService, Context context){
            this.jobservice = jobService;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            Log.e(TAG, "PREEXECUTE");
            super.onPreExecute();
            mDetectionActivity = new DetectionActivity();
        }

        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            Log.e(TAG, "BACK");
            mDetectionActivity.getClient().connect();
            return jobParameters[0];
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            Log.e(TAG, "FINISH");
            super.onPostExecute(jobParameters);
            jobservice.jobFinished(jobParameters, false);
        }
    }


}
