package com.example.android.sampletvinput.Model;

import android.content.Context;
import android.util.Log;

import com.example.android.sampletvinput.JsonParser.Parser;
import com.google.android.media.tv.companionlibrary.model.Channel;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Tamim Baschour on 03.03.2017.
 */

public class ChannelsDB {

    private static final String TAG = ChannelsDB.class.getSimpleName();

    private static long MAX_AGE = 43200000;
    private static ChannelsDB myChannelsDB;
    List<Channel> channels;
    private long lastUpdate;

    private Context context;


    /** The default period between full EPG syncs, one day. */
    private static final long DEFAULT_SYNC_PERIOD_MILLIS = 1000 * 60 * 60 * 12; // 12 hour
    private static final long DEFAULT_IMMEDIATE_EPG_DURATION_MILLIS = 1000 * 60 * 60; // 1 Hour
    private static final long DEFAULT_PERIODIC_EPG_DURATION_MILLIS = 1000 * 60 * 60 * 48; // 48 Hour

    public static ChannelsDB getInstance(Context context) {
        if (myChannelsDB == null) {
            myChannelsDB = new ChannelsDB(context);
        }
        return myChannelsDB;
    }

    private ChannelsDB(Context context){
        Log.d(TAG, "new ChannelsDB Instance");
        this.context = context;
    }


    public List<Channel> getChannels() {
        if (channels == null) {
            getChannels(true);
            Log.d(TAG, "channles is null... refreshing");
        } else if ((lastUpdate + MAX_AGE) <= (new Date().getTime())) {
            getChannels(true);
            Log.d(TAG, "channles too old... refreshing");
        }
        Log.d(TAG, "Found " + channels.size() + " Channels");

        return channels;
    }

    public List<Channel> getChannels(Boolean forceRefresh) {
        if (forceRefresh)
            try {
                channels = new ArrayList<>(new Parser().getChannels());
                lastUpdate = new Date().getTime();
                Log.d(TAG, "ChannelDB Timestamp: " + lastUpdate);
            } catch (JSONException | IOException e) { e.printStackTrace(); }

        if(channels == null)
            getChannels(true);

        return channels;
    }

    public void reset() {
        channels = null;
    }

}
