package com.example.android.sampletvinput.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.android.sampletvinput.JsonParser.Parser;
import com.google.android.media.tv.companionlibrary.model.Channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Tamim Baschour on 03.03.2017.
 */

public class ChannelsDB {

    private static final String TAG = ChannelsDB.class.getSimpleName();

    private static long MAX_AGE = 43200000;
    private static ChannelsDB myChannelsDB;
    private HashMap<Integer, Channel> channelMap;
    private long lastUpdate;

    private Context context;


    /**
     * The default period between full EPG syncs, one day.
     */
    private static final long DEFAULT_SYNC_PERIOD_MILLIS = 1000 * 60 * 60 * 12; // 12 hour
    private static final long DEFAULT_IMMEDIATE_EPG_DURATION_MILLIS = 1000 * 60 * 60; // 1 Hour
    private static final long DEFAULT_PERIODIC_EPG_DURATION_MILLIS = 1000 * 60 * 60 * 48; // 48 Hour

    public static ChannelsDB getInstance(Context context) {
        if (myChannelsDB == null) {
            myChannelsDB = new ChannelsDB(context);
        }
        return myChannelsDB;
    }

    @SuppressLint("UseSparseArrays")
    private ChannelsDB(Context context) {
        Log.d(TAG, "new ChannelsDB Instance");
        this.context = context;
        channelMap = new HashMap<>();
    }


    //TODO: do networking and parsing in background service
    public List<Channel> getChannels() {

        List<Channel> channels = new ArrayList<>();

        if (channelMap.isEmpty() || (lastUpdate + MAX_AGE) <= (new Date().getTime())) {
            Log.d(TAG, "refreshing channels");
            channels.addAll(new Parser().getChannels());
            lastUpdate = new Date().getTime();
            Log.d(TAG, "ChannelDB Timestamp: " + lastUpdate);

            List<Integer> keyList = new ArrayList<>();
            for (Channel channel : channels) {
                keyList.add(channel.getOriginalNetworkId());
                channelMap.put(channel.getOriginalNetworkId(), channel);
            }
            channelMap.keySet().retainAll(keyList);
        }

        Log.d(TAG, "Found " + channelMap.size() + " Channels");


        return new ArrayList<>(channelMap.values());
    }
}
