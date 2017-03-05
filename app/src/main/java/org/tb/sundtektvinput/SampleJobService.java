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
package org.tb.sundtektvinput;

import android.net.Uri;
import android.util.Log;

import org.tb.sundtektvinput.Model.ChannelsDB;
import org.tb.sundtektvinput.Model.ProgramsDB;
import com.google.android.exoplayer.util.Util;
import com.google.android.media.tv.companionlibrary.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * EpgSyncJobService that periodically runs to update channels and programs.
 */
public class SampleJobService extends EpgSyncJobService {

    private static String TAG = SampleJobService.class.getSimpleName();


    @Override
    public List<Channel> getChannels() {
        return ChannelsDB.getInstance(getApplicationContext()).getChannels();
    }


    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channel, long startMs,
                                               long endMs) {

        Log.d(TAG, "Trying to get programs for " + channel.getDisplayName());
        return ProgramsDB.getInstance(getApplicationContext())
                .getProgramsForChannel(channel)
                .get(String.valueOf(channel.getOriginalNetworkId()));
    }


    private static final String PROG_IPD_EPG_EVENT_ID = "epgEventId";
    private static final String PROG_IPD_SERVICE_ID = "serviceId";
    private static final String PROG_IPD_ORIGINAL_NETWORK_ID = "originalNetworkId";
    private static final String PROG_IPD_CHANNEL_NAME = "channelName";

    private ArrayList<Program> makeDummyProgram(Channel channel) {

        InternalProviderData ipd = new InternalProviderData();
        ipd.setVideoType(Util.TYPE_OTHER);
        ipd.setRepeatable(true);
        try {
            ipd.setVideoUrl((String) channel.getInternalProviderData().get("mediaUrl"));
            ipd.put(PROG_IPD_SERVICE_ID, channel.getServiceId());
            ipd.put(PROG_IPD_ORIGINAL_NETWORK_ID, channel.getOriginalNetworkId());
            //set default eventId since there are no real events for the channel
            ipd.put(PROG_IPD_EPG_EVENT_ID, channel.getOriginalNetworkId() + "/" + channel.getOriginalNetworkId());
            ipd.put(PROG_IPD_CHANNEL_NAME, channel.getDisplayName());
        } catch (InternalProviderData.ParseException e) {
            e.printStackTrace();
        }

        ArrayList<Program> dummy = new ArrayList<>();

        dummy.add(new Program.Builder()
                .setTitle(channel.getDisplayName())
                .setThumbnailUri(channel.getChannelLogo())
                .setInternalProviderData(ipd)
                .setStartTimeUtcMillis(new Date().getTime())
                .setEndTimeUtcMillis(new Date().getTime() + 86400000) //24h
                .build());

        Log.d(TAG, "created dummy program info for channel: " + channel.getDisplayName());
        return dummy;
    }

}
