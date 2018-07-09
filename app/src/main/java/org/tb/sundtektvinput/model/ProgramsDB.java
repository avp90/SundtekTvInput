package org.tb.sundtektvinput.model;

import android.content.Context;
import android.util.Log;

import com.google.android.exoplayer.util.Util;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.tb.sundtektvinput.SundtekTvInputApp;
import org.tb.sundtektvinput.parser.SundtekJsonParser;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Tamim Baschour on 27.02.2017.
 */

public class ProgramsDB {

    private static final String TAG = ProgramsDB.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static ProgramsDB myProgramsDB;
    //allProgramMap eventid-program
    private final HashMap<String, Program> allProgramMap;
    //channelProgramsMap channelNetworkId-program
    private final HashMap<String, ArrayList<Program>> channelProgramsMap;
    private String lastUpdateList;
    private String activeList;

    private long lastUpdate;
    private static long MAX_AGE_MILLIS = 1000 * 60 * 60 * 1; // 1h


    private static final String PROG_IPD_EPG_EVENT_ID = "epgEventId";
    private static final String PROG_IPD_SERVICE_ID = "serviceId";
    private static final String PROG_IPD_ORIGINAL_NETWORK_ID = "originalNetworkId";
    private static final String PROG_IPD_CHANNEL_NAME = "channelName";


    public static ProgramsDB getInstance() {
        if (myProgramsDB == null) {
            myProgramsDB = new ProgramsDB();
        }
        return myProgramsDB;
    }

    private ProgramsDB() {
        if (DEBUG)
            Log.d(TAG, "new ProgramsDB Instance");
        allProgramMap = new HashMap<>();
        channelProgramsMap = new HashMap<>();
        lastUpdateList = new SettingsHelper(SundtekTvInputApp.getContext()).loadSelectedList();
        lastUpdate = new SettingsHelper(SundtekTvInputApp.getContext()).loadLastUpdateTimestamp();
    }


    //TODO: Only get programs for given channel when sundtek api is ready
    private void getPrograms(Context context, Channel channel, long startMs, long endMs) {
        channelProgramsMap.clear();
        allProgramMap.clear();

        ArrayList<Program> programs = new ArrayList<>();
        SundtekJsonParser parser = new SundtekJsonParser(context);

        Log.d(TAG, "fetch new EPG data from server");

        programs.addAll(parser.getPrograms(SundtekJsonParser.EPG_MODE_NOW, true, activeList));
        programs.addAll(parser.getPrograms(SundtekJsonParser.EPG_MODE_TODAY, false, activeList));
        programs.addAll(parser.getPrograms(SundtekJsonParser.EPG_MODE_TOMORROW, false, activeList));

        String channelId;
        String eventId;
        String newEventId;
        String channelName;
        InternalProviderData ipd;
        boolean newProgram;
        int newCount = 0;
        int replacedCount = 0;
        for (Program program : programs) {
            try {
                ipd = program.getInternalProviderData();
                channelName = String.valueOf(ipd.get(PROG_IPD_CHANNEL_NAME));
                channelId = (String) ipd.get(PROG_IPD_ORIGINAL_NETWORK_ID);
                eventId = (String) ipd.get(PROG_IPD_EPG_EVENT_ID);

                // Event ids are not unique so we pair them with the corresponding originalNetworkId
                newEventId = eventId + "/" + channelId;
            } catch (InternalProviderData.ParseException e) {
                channelName = "error";
                channelId = "error";
                newEventId = "error";
                e.printStackTrace();
            }

            if (!channelProgramsMap.containsKey(channelId))
                channelProgramsMap.put(channelId, new ArrayList<Program>());
            channelProgramsMap.get(channelId).add(program);
            newProgram = allProgramMap.put(newEventId, program) == null;
            if (newProgram) newCount++;
            else replacedCount++;

            if (DEBUG)
                Log.d(TAG, channelName + " - EventId: " + newEventId + " : " + (newProgram ? "new" : "replaced"));
        }

        Log.d(TAG, "Got " + allProgramMap.size() + " Programs for " + channelProgramsMap.values().size() + " channels from server");

        if (DEBUG) {
            Log.d(TAG, "Programs " + "new: " + newCount + " replaced: " + replacedCount + " total: " + (newCount + replacedCount));

            for (String key : channelProgramsMap.keySet()) {
                Log.d(TAG, "channelId " + key + " - " + channelProgramsMap.get(key).size() + " programs");
            }
        }
    }


    public ArrayList<Program> getProgramsForChannel(Context context, Channel channel, long startMs, long endMs) {
        activeList = new SettingsHelper(SundtekTvInputApp.getContext()).loadSelectedList();
        boolean fullSync = false;
        if (channelProgramsMap.isEmpty() || ((lastUpdate + MAX_AGE_MILLIS) <= (System.currentTimeMillis())) || !lastUpdateList.equals(activeList)) {

            Log.d(TAG,
                    "Reasons for EPG update:" +
                            "\n new list: \t" + (!activeList.equals(lastUpdateList)) +
                            "\n DB empty: \t" + channelProgramsMap.isEmpty() +
                            "\n data too old: \t" + ((lastUpdate + MAX_AGE_MILLIS) <= System.currentTimeMillis())
            );

            lastUpdate = System.currentTimeMillis();
            new SettingsHelper(SundtekTvInputApp.getContext()).saveLastUpdateTimestamp(lastUpdate);
            getPrograms(context, channel, startMs, endMs);
            lastUpdateList = activeList;
        }

        String channelId = String.valueOf(channel.getOriginalNetworkId());

        ArrayList<Program> programList = channelProgramsMap.get(channelId);
        if (programList == null)
            programList = new ArrayList<>();

        if (DEBUG)
            Log.d(TAG, "return " + programList.size() + " programs for " + channel.getDisplayName());

        return programList;
    }

    public Program getDummyProgram(Channel channel) {
        return makeDummyProgram(channel, System.currentTimeMillis(), 0).get(0);
    }

    private ArrayList<Program> makeDummyProgram(Channel channel, long startMs, long endMs) {

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
                .setStartTimeUtcMillis(startMs)
                .setEndTimeUtcMillis(startMs + 60 * 30 * 1000) //duration 10min
                .build());

        if (DEBUG)
            Log.d(TAG, "created dummy program info for channel: " + channel.getDisplayName());

        return dummy;
    }
}
