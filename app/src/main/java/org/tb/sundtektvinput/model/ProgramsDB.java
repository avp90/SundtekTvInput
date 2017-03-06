package org.tb.sundtektvinput.model;

import android.util.Log;

import com.google.android.exoplayer.util.Util;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.tb.sundtektvinput.parser.SundtekJsonParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tamim Baschour on 27.02.2017.
 */

public class ProgramsDB {

    private static final String TAG = ProgramsDB.class.getSimpleName();

    private static ProgramsDB myProgramsDB;
    private SundtekJsonParser parser;
    private HashMap<String, Program> allProgramMap;
    private HashMap<String, ArrayList<Program>> channelProgramsMap;

    private long lastUpdate;
    private static long MAX_AGE_MILLIS = 1000 * 60 * 60 * 1 / 12; //5mins


    private static final String PROG_IPD_EPG_EVENT_ID = "epgEventId";
    private static final String PROG_IPD_SERVICE_ID = "serviceId";
    private static final String PROG_IPD_ORIGINAL_NETWORK_ID = "originalNetworkId";
    private static final String PROG_IPD_CHANNEL_NAME = "channelName";


    public static ProgramsDB getInstance() {
        if (myProgramsDB == null) {
            myProgramsDB = new ProgramsDB();
            myProgramsDB.parser = new SundtekJsonParser();
        }
        return myProgramsDB;
    }

    private ProgramsDB() {
        Log.d(TAG, "new ProgramsDB Instance");
        allProgramMap = new HashMap<>();

    }


    //TODO: do networking and parsing in background service
    private boolean getPrograms() {

        if (allProgramMap.isEmpty() || (lastUpdate + MAX_AGE_MILLIS) <= (new Date().getTime())) {
            List<Program> programs;
            programs = parser.getPrograms();
            lastUpdate = new Date().getTime();
            Log.d(TAG, "refreshing programs");

            List<String> keyList = new ArrayList<>();
            String eventId = "error";

            for (Program program : programs) {
                try {

                    InternalProviderData ipd = program.getInternalProviderData();

                    // Event ids are not unique so we pair them with the corresponding originalNetworkId
                    eventId = ipd.get(PROG_IPD_EPG_EVENT_ID) + "/" + ipd.get(PROG_IPD_ORIGINAL_NETWORK_ID);

                    if (eventId.equals(ipd.get(PROG_IPD_ORIGINAL_NETWORK_ID) + "/" + ipd.get(PROG_IPD_ORIGINAL_NETWORK_ID)))
                        Log.d(TAG, "No program data for " + ipd.get(PROG_IPD_CHANNEL_NAME));

                } catch (InternalProviderData.ParseException e) {
                    e.printStackTrace();
                }
                keyList.add(eventId);
                allProgramMap.put(eventId, program);
            }
            allProgramMap.keySet().retainAll(keyList);
            Log.d(TAG, "Found " + allProgramMap.size() + " Programs");

            channelProgramsMap = new HashMap<>();

            for (Program program : allProgramMap.values()) {
                try {
                    String key = (String) program.getInternalProviderData().get(PROG_IPD_ORIGINAL_NETWORK_ID);

                    if (!channelProgramsMap.containsKey(key))
                        channelProgramsMap.put(key, new ArrayList<Program>());

                    channelProgramsMap.get(key).add(program);

                } catch (InternalProviderData.ParseException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "found programs for " + channelProgramsMap.values().size() + "channels");

            return true;
        }

        return false;
    }

    public ArrayList<Program> getProgramsForChannel(Channel channel, long startMs, long endMs) {

        getPrograms();

        String channelKey = String.valueOf(channel.getOriginalNetworkId());

//        if (!channelProgramsMap.containsKey(channelKey)) {
//            Log.d(TAG, "No programdata found for channel: " + channel.getDisplayName());
//            return makeDummyProgram(channel, startMs, endMs);
//        } else if (channelProgramsMap.get(channelKey) == null || channelProgramsMap.get(channelKey).isEmpty()) {
//            channelProgramsMap.remove(channelKey);
//            Log.d(TAG, "Removed empty programlist from Map for: " + channel.getDisplayName());
//            return makeDummyProgram(channel, startMs, endMs);
//        }
//
        ArrayList<Program> programList = channelProgramsMap.get(channelKey);
        if (!(channelProgramsMap.get(channelKey) == null))
            Log.d(TAG, "found " + channelProgramsMap.get(channelKey).size() + " programs for " + channel.getDisplayName());
//
//
          // TODO: Implement method in ProgramsDB to get program by time for given channel
          // Find a single program for the given time. If there is none a dummyprogram will be added
//        Boolean hasProgramForGivenTime= false;
//        for(Program program : programList) {
//            if (program.getEndTimeUtcMillis() >= startMs && program.getStartTimeUtcMillis() <= startMs) {
//                hasProgramForGivenTime = true;
//                break;
//            }
//        }
//        if(!hasProgramForGivenTime)
//            programList.add(makeDummyProgram(channel, startMs, endMs).get(0));
//
//
        return programList;
    }

    public Program getDummyProgram(Channel channel){
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
                .setEndTimeUtcMillis(startMs  + 60 * 30 * 1000) //duration 10min
                .build());

        Log.d(TAG, "created dummy program info for channel: " + channel.getDisplayName());

        return dummy;
    }

}
