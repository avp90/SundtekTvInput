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
    private static final boolean DEBUG = false;

    private static ChannelsDB myChannelsDB;
    private HashMap<Long, Channel> channelMap;


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
        if (DEBUG)
            Log.d(TAG, "new ChannelsDB Instance");
        channelMap = new HashMap<>();
    }


    public List<Channel> getChannels(Context context) {
        SettingsHelper helper = new SettingsHelper(context);
        String activeList = helper.loadSelectedList();
        ArrayList<Long> filter = helper.loadSelectedChannels(activeList);

        List<Channel> channels = new ArrayList<>();

        Log.d(TAG, "refreshing channels");
        channels.addAll(new SundtekJsonParser(context).getChannels(activeList));

        channelMap.clear();

        for (Channel channel : channels) {
            long key = channel.getOriginalNetworkId();
            if (filter.contains(key)) {
                channelMap.put(key, channel);
            }
        }
        Log.d(TAG, "Found " + channelMap.size() + " Channels");

        return new ArrayList<>(channelMap.values());

    }
}
