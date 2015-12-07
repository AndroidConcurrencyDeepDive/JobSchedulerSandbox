/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package net.callmeike.android.jobscheduler.scheduler;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.util.SparseArray;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import net.callmeike.android.jobscheduler.R;
import net.callmeike.android.jobscheduler.tasks.DaggerTaskComponent;
import net.callmeike.android.jobscheduler.tasks.SampleTask;

import dagger.Lazy;


/**
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 * @version $Revision: $
 */
public class SimpleJobService extends JobService {
    private static final String TAG = "SCHEDULER";

    static final int MSG_START_TASK = -1;

    private static final String PARAM_TASK_TYPE = "SimpleJobService.TASK";
    private static final int SAMPLE_TASK = -1;

    private static final AtomicInteger JOB_ID = new AtomicInteger();

    public static void startSampleTask(Context ctxt) {
        JobScheduler js = ((JobScheduler) ctxt.getSystemService(Context.JOB_SCHEDULER_SERVICE));

        cancelAllSampleTasks(js);

        PersistableBundle extras = new PersistableBundle();
        extras.putInt(PARAM_TASK_TYPE, SAMPLE_TASK);

        Resources rez = ctxt.getResources();
        int intervalSecs = 1000 * rez.getInteger(R.integer.sample_task_interval);

        js.schedule(
            new JobInfo.Builder(
                JOB_ID.getAndIncrement(),
                new ComponentName(ctxt, SimpleJobService.class))
                .setExtras(extras)
                .setBackoffCriteria(intervalSecs / 2, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setPeriodic(intervalSecs)
                .setPersisted(true)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .build());
    }

    public static void cancelAllSampleTasks(Context ctxt) {
        cancelAllSampleTasks((JobScheduler) ctxt.getSystemService(Context.JOB_SCHEDULER_SERVICE));
    }

    private static void cancelAllSampleTasks(JobScheduler js) {
        List<JobInfo> jobs = js.getAllPendingJobs();
        for (JobInfo job : jobs) {
            PersistableBundle extras = job.getExtras();
            if ((null != extras) && (extras.getInt(PARAM_TASK_TYPE, 0) == SAMPLE_TASK)) {
                js.cancel(job.getId());
            }
        }
    }


    private final SparseArray<JobParameters> tasks = new SparseArray<>();

    @Inject
    volatile Lazy<SampleTask> sampleTask;

    private Handler taskHandler;
    private int currentTask;

    @Override
    @UiThread
    @SuppressWarnings("HandlerLeak")
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("JobService");
        thread.start();

        DaggerTaskComponent.create().inject(this);

        taskHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case SimpleJobService.MSG_START_TASK:
                        startTask((JobParameters) msg.obj);
                        break;
                    default:
                        Log.w(TAG, "unexpected message: " + msg.what);
                }
            }
        };
    }

    @Override
    @UiThread
    public void onDestroy() {
        taskHandler.getLooper().quitSafely();
        super.onDestroy();
    }

    @Override
    @UiThread
    public boolean onStartJob(JobParameters params) {
        enqueueTask(params);
        return true;
    }

    @Override
    @UiThread
    public boolean onStopJob(JobParameters params) {
        cancelTask(params);
        return true;
    }

    @WorkerThread
    void startTask(JobParameters params) {
        int op = dequeueTask(params);
        try {
            switch (op) {
                case SimpleJobService.SAMPLE_TASK:
                    if (!Thread.currentThread().isInterrupted()) { sampleTask.get().run(); }
                    break;
                default:
                    Log.w(TAG, "unexpected op: " + op);
            }
        }
        finally {
            jobFinished(params, false);
        }
    }

    @UiThread
    private void enqueueTask(JobParameters params) {
        int jobId = params.getJobId();
        synchronized (tasks) {
            if (null != tasks.get(jobId)) {
                throw new IllegalStateException("job id is not unique: " + jobId);
            }
            tasks.put(params.getJobId(), params);
            taskHandler.obtainMessage(MSG_START_TASK, params).sendToTarget();
        }
    }

    @UiThread
    private void cancelTask(JobParameters params) {
        int jobId = params.getJobId();
        synchronized (tasks) {
            if (currentTask == jobId) { taskHandler.getLooper().getThread().interrupt(); }
            else {
                JobParameters oParams = tasks.get(jobId);
                if (null == oParams) { return; }
                tasks.delete(jobId);
                taskHandler.removeMessages(MSG_START_TASK, oParams);
            }
        }
    }

    @WorkerThread
    private int dequeueTask(JobParameters params) {
        int jobId = params.getJobId();

        PersistableBundle extras = params.getExtras();
        if (null == extras) {
            Log.w(TAG, "null extras");
            return 0;
        }

        synchronized (tasks) {
            if (null == tasks.get(jobId)) { return 0; }
            tasks.delete(jobId);
            currentTask = jobId;
            Thread.interrupted(); // clear thread interrupts
        }

        return extras.getInt(SimpleJobService.PARAM_TASK_TYPE, 0);
    }
}
