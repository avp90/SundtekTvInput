package org.tb.sundtektvinput.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.SundtekTvInputApp;
import org.tb.sundtektvinput.parser.SundtekJsonParser;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tamim Baschour on 03.03.2017.
 * The default period between full EPG syncs, one day.
 */
public class ChannelsDB {
    private static final String TAG = ChannelsDB.class.getSimpleName();
    private static final boolean DEBUG = false;
    protected SundtekTvInputApp app;
    private HashMap<Long, Channel> channelMap;

    @SuppressLint("UseSparseArrays")
    public ChannelsDB(SundtekTvInputApp app) {
        if (DEBUG) Log.d(TAG, "new ChannelsDB Instance");
        this.app = app;
        channelMap = new HashMap<>();
    }

    public List<Channel> getChannels(Context context) {
        SettingsHelper helper = new SettingsHelper(context);
        String activeList = helper.loadSelectedList();
        ArrayList<Long> filter = helper.loadSelectedChannels(activeList);

        Log.d(TAG, "refreshing channels");
        List<Channel> channels =
                new ArrayList<>(app.getSundtekJsonParser().getChannels(activeList));

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
