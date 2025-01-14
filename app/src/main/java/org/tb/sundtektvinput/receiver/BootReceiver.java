/*
 * Copyright 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tb.sundtektvinput.receiver;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.tb.sundtektvinput.service.EpgJobService;
import org.tb.sundtektvinput.service.base.EpgSyncJobService;

import java.util.List;

/**
 * This BroadcastReceiver is set up to make sure sync job can schedule after reboot. Because
 * JobScheduler doesn't work well on reboot scheduler on L/L-MR1.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        // If there are not pending jobs. Create a sync job and schedule it.
        List<JobInfo> pendingJobs = jobScheduler.getAllPendingJobs();
        Log.d(this.getClass().getSimpleName(), "Found pending jobs: " + pendingJobs.toString());
        if (pendingJobs.isEmpty()) {
            String inputId = context.getSharedPreferences(EpgSyncJobService.PREFERENCE_EPG_SYNC,
                    Context.MODE_PRIVATE).getString(EpgSyncJobService.BUNDLE_KEY_INPUT_ID, null);
            if (inputId != null) {
                // Set up periodic sync only when input has set up.
                EpgSyncJobService.setUpPeriodicSync(context, inputId,
                        new ComponentName(context, EpgJobService.class));
                Log.d(this.getClass().getSimpleName(), "Setup periodic sync with inputId " + inputId);

            } else {
                Log.d(this.getClass().getSimpleName(), "Can't setup periodic sync cause inputId is null");
            }

            return;
        }
        // On L/L-MR1, reschedule the pending jobs.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            for (JobInfo job : pendingJobs) {
                if (job.isPersisted()) {
                    jobScheduler.schedule(job);
                }
            }
        }
    }
}
