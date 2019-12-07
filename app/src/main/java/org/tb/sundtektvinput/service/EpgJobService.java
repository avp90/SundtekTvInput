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
package org.tb.sundtektvinput.service;

import android.net.Uri;
import android.util.Log;
import java.util.List;

import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.tb.sundtektvinput.SundtekTvInputApp;
import org.tb.sundtektvinput.service.base.EpgSyncJobService;


/**
 * EpgSyncJobService that periodically runs to update channels and programs.
 */
public class EpgJobService extends EpgSyncJobService {
    private static String TAG = EpgJobService.class.getSimpleName();
    private static final boolean DEBUG = true;
    public SundtekTvInputApp app = null;

    @Override
    public void onCreate() {
        super.onCreate();
        app = (SundtekTvInputApp)getApplication();
    }

    public SundtekTvInputApp getApp() {
        return (SundtekTvInputApp)getApplication();
    }

    @Override
    public List<Channel> getChannels() {
        return getApp().getChannelsDB().getChannels(getApplicationContext());
    }

    @Override
    public List<Program> getProgramsForChannel(
            Uri channelUri, Channel channel, long startMs, long endMs) {
        if (DEBUG) Log.d(TAG, "Trying to get programs for " + channel.getDisplayName());

        List<Program> programs = getApp().getProgramsDB()
                .getProgramsForChannel(getApplicationContext(), channel, startMs, endMs);

        if (DEBUG)
            Log.d(TAG,
                    "got "
                            + (programs == null ? "no" : programs.size())
                            + " programs for "
                            + channel.getDisplayName());

        return programs;
    }
}
