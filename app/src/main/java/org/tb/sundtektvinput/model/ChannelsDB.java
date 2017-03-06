package org.tb.sundtektvinput.model;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.parser.SundtekJsonParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Tamim Baschour on 03.03.2017.
 */

public class ChannelsDB {

    private static final String TAG = ChannelsDB.class.getSimpleName();

    private static long MAX_AGE = 1000 * 60 * 60   / 12; //5mins;
    private static ChannelsDB myChannelsDB;
    private HashMap<Integer, Channel> channelMap;
    private long lastUpdate;


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


    //TODO: do networking and parsing in background service
    public List<Channel> getChannels() {

        List<Channel> channels = new ArrayList<>();

        if (channelMap.isEmpty() || (lastUpdate + MAX_AGE) <= (new Date().getTime())) {
            Log.d(TAG, "refreshing channels");
            channels.addAll(new SundtekJsonParser().getChannels());
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

    public HashMap<Integer, Channel> getChannelMap(){
        return channelMap;
    }
}
