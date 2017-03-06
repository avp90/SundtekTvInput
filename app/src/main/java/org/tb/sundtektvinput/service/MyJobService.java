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

import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.tb.sundtektvinput.model.ChannelsDB;
import org.tb.sundtektvinput.model.ProgramsDB;
import org.tb.sundtektvinput.service.base.EpgSyncJobService;

import java.util.List;

/**
 * EpgSyncJobService that periodically runs to update channels and programs.
 */
public class MyJobService extends EpgSyncJobService {

    private static String TAG = MyJobService.class.getSimpleName();


    @Override
    public List<Channel> getChannels() {
        return ChannelsDB.getInstance(getApplicationContext()).getChannels();
    }


    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channel, long startMs,
                                               long endMs) {

        Log.d(TAG, "Trying to get programs for " + channel.getDisplayName());
        return ProgramsDB.getInstance().getProgramsForChannel(channel, startMs, endMs);
    }


}
