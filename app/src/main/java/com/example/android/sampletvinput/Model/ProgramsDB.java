package com.example.android.sampletvinput.Model;

import android.util.Log;

import com.example.android.sampletvinput.JsonParser.Parser;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

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
    private Parser parser;
    private HashMap<String, Program> allProgramMap;
    private HashMap<String, ArrayList<Program>> channelProgramsMap;

    private long lastUpdate;
    private static long MAX_AGE_MILLIS = 43200000;


    private static final String PROG_IPD_EPG_EVENT_ID = "epgEventId";
    private static final String PROG_IPD_SERVICE_ID = "serviceId";
    private static final String PROG_IPD_ORIGINAL_NETWORK_ID = "originalNetworkId";
    private static final String PROG_IPD_CHANNEL = "channel";


    public static ProgramsDB getInstance() {
        if (myProgramsDB == null) {
            myProgramsDB = new ProgramsDB();
            myProgramsDB.parser = new Parser();
        }
        return myProgramsDB;
    }

    private ProgramsDB() {
        Log.d(TAG, "new ProgramsDB Instance");
        allProgramMap = new HashMap<>();
    }


    //TODO: do networking and parsing in background service
    public List<Program> getPrograms(Channel channel) {

        if (allProgramMap.isEmpty() || (lastUpdate + MAX_AGE_MILLIS) <= (new Date().getTime())) {
            List<Program> programs;
            programs = parser.getPrograms();
            lastUpdate = new Date().getTime();
            Log.d(TAG, "refreshing programs");

            List<String> keyList = new ArrayList<>();
            String eventId = "error";

            for (Program program : programs) {
                try {
                    // Event ids are not unique so we pair them with the corresponding originalNetworkId
                    eventId = program.getInternalProviderData().get(PROG_IPD_EPG_EVENT_ID) + "/" + program.getInternalProviderData().get(PROG_IPD_ORIGINAL_NETWORK_ID);
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
        }


        return getProgramsForChannel(channel);
    }

    private List<Program> getProgramsForChannel(Channel channel) {
        String channelKey = String.valueOf(channel.getOriginalNetworkId());

        if (!channelProgramsMap.containsKey(channelKey))
            channelProgramsMap.put(channelKey, new ArrayList<Program>());

        Log.d(TAG, "found " + (channelProgramsMap.get(channelKey) != null ? channelProgramsMap.get(channelKey).size() : 0) + " programs for " + channel.getDisplayName());


        return channelProgramsMap.get(channelKey);
    }


}
