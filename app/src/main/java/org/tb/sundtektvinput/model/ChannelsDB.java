package org.tb.sundtektvinput.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.parser.SundtekJsonParser;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Tamim Baschour on 03.03.2017.
 */

public class ChannelsDB {

    private static final String TAG = ChannelsDB.class.getSimpleName();

    private static long MAX_AGE = 1000 * 60 * 60 / 12; //5mins;
    private static ChannelsDB myChannelsDB;
    private HashMap<Long, Channel> channelMap;
    private long lastUpdate;
    private ArrayList<Long> filter;


    /**
     * The default period between full EPG syncs, one day.
     */

    public static ChannelsDB getInstance() {
        if (myChannelsDB == null) {
            myChannelsDB = new ChannelsDB();
        }
        return myChannelsDB;
    }

    @SuppressLint("UseSparseArrays")
    private ChannelsDB() {
        Log.d(TAG, "new ChannelsDB Instance");
        channelMap = new HashMap<>();
    }


    public List<Channel> getChannels(Context context) {

        filter = new SettingsHelper(context).loadSelectedChannelsMap();
        List<Channel> channels = new ArrayList<>();

        Log.d(TAG, "refreshing channels");
        channels.addAll(new SundtekJsonParser(context).getChannels());
        lastUpdate = System.currentTimeMillis();

        Log.d(TAG, "ChannelDB Timestamp: " + lastUpdate);

        channelMap.clear();

//        Channel newChannel;
        for (Channel channel : channels) {
            long key = channel.getOriginalNetworkId();
            if (filter.contains(key)) {
//                newChannel = new Channel.Builder(channel).build();
                channelMap.put(key, channel);
                Log.d(TAG, "Found " + channelMap.size() + " Channels");
            }
        }
        return new ArrayList<>(channelMap.values());

    }
}
