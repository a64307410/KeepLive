package com.hinnka.keepalive.component;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.hinnka.keepalive.KeepAliveConfig;

import java.util.concurrent.TimeUnit;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class KeepAliveJobService extends JobService {

    public static final String TAG = "JobSchedulerService";
    public static long minTime = (TimeUnit.MINUTES.toMillis(Build.VERSION.SDK_INT >= 23 ? 10 : 1) - TimeUnit.SECONDS.toMillis(10));

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.i(TAG, "JobSchedulerService onStartJob");
        int jobId = jobParameters.getJobId();
        if (jobId == 206) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    jobFinished(jobParameters, false);
                    start(KeepAliveJobService.this, 206);
                }
            }, minTime);
        } else {
            start(this, jobId);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i(TAG, "JobSchedulerService onStopJob");
        return false;
    }

    public static void start(Context context, int i) {
        try {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            JobInfo jobInfo = null;
            if (Build.VERSION.SDK_INT >= 24) {
                jobInfo = jobScheduler.getPendingJob(i);
            } else {
                for (JobInfo next : jobScheduler.getAllPendingJobs()) {
                    if (next.getId() == i) {
                        jobInfo = next;
                    }
                }
            }
            if (jobInfo != null) {
                jobScheduler.cancel(i);
            }
            JobInfo.Builder builder = new JobInfo.Builder(i, new ComponentName(context, KeepAliveJobService.class));
            switch (i) {
                case 203:
                    builder.setMinimumLatency(minTime);
                    builder.setRequiresDeviceIdle(true);
                    break;
                case 204:
                    builder.setMinimumLatency(minTime);
                    builder.setRequiresCharging(true);
                    break;
                case 205:
                    builder.setMinimumLatency(minTime);
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
                    break;
                case 206:
                    builder.setMinimumLatency(10000);
                    builder.setOverrideDeadline(10000);
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                    builder.setRequiresDeviceIdle(false);
                    builder.setRequiresCharging(false);
                    break;
                default:
                    return;
            }
            builder.setPersisted(true);
            jobScheduler.schedule(builder.build());
        } catch (Exception unused) {
        }
    }
}
