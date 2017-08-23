package org.tb.sundtektvinput.model;

import android.content.Context;
import android.media.tv.TvContract;
import android.os.Build;
import android.util.Log;

import com.google.android.exoplayer.util.Util;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.tb.sundtektvinput.parser.SundtekJsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tamim Baschour on 27.02.2017.
 */

public class ProgramsDB {

    private static final String TAG = ProgramsDB.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static ProgramsDB myProgramsDB;
    //allProgramMap eventid-program
    private final HashMap<String, Program> allProgramMap;
    //channelProgramsMap channelNetworkId-program
    private final HashMap<String, ArrayList<Program>> channelProgramsMap;

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
        Log.d(TAG, "new ProgramsDB Instance");
        allProgramMap = new HashMap<>();
        channelProgramsMap = new HashMap<>();
    }


    //TODO: Only get programs for given channel when sundtek api is ready
    private boolean getPrograms(Context context, Channel channel, long startMs, long endMs) {
        lastUpdate = System.currentTimeMillis();

        channelProgramsMap.clear();
        allProgramMap.clear();

        ArrayList<Program> programs = new ArrayList<>();
        SundtekJsonParser parser = new SundtekJsonParser(context);

        programs.addAll(parser.getPrograms(SundtekJsonParser.EPG_MODE_TODAY));
        programs.addAll(parser.getPrograms(SundtekJsonParser.EPG_MODE_TOMORROW));

        Log.d(TAG, "refreshing programs");

        String channelId;
        String eventId;
        String newEventId;
        InternalProviderData ipd;

        for (Program program : programs) {
            try {
                ipd = program.getInternalProviderData();
                channelId = (String) ipd.get(PROG_IPD_ORIGINAL_NETWORK_ID);
                eventId = (String) ipd.get(PROG_IPD_EPG_EVENT_ID);

                // Event ids are not unique so we pair them with the corresponding originalNetworkId
                newEventId = eventId + "/" + channelId;
                if (DEBUG)
                    Log.d(TAG, ipd.get(PROG_IPD_CHANNEL_NAME) + " - EventId: " + newEventId);

            } catch (InternalProviderData.ParseException e) {
                channelId = "error";
                newEventId = "error";
                e.printStackTrace();
            }

            if (!channelProgramsMap.containsKey(channelId))
                channelProgramsMap.put(channelId, new ArrayList<Program>());
            channelProgramsMap.get(channelId).add(program);
            allProgramMap.put(newEventId, program);
        }

        if (DEBUG) {
            Log.d(TAG, "Got " + allProgramMap.size() + " Programs for " + channelProgramsMap.values().size() + " channels");
        }
        return true;
    }


    public ArrayList<Program> getProgramsForChannel(Context context, Channel channel, long startMs, long endMs) {
        if (channelProgramsMap.isEmpty() || ((lastUpdate + MAX_AGE_MILLIS) <= (System.currentTimeMillis()))) {
            if (DEBUG) {
                Log.d(TAG, "channelProgramsMap.isEmpty(): " + channelProgramsMap.isEmpty());
                Log.d(TAG, "data too old: " + (lastUpdate + MAX_AGE_MILLIS <= System.currentTimeMillis()));
            }
            getPrograms(context, channel, startMs, endMs);
        }

        String channelId = String.valueOf(channel.getOriginalNetworkId());

        ArrayList<Program> programList = channelProgramsMap.get(channelId);
        if (programList == null)
            programList = new ArrayList<>();

        if (DEBUG)
            Log.d(TAG, "found " + programList.size() + " programs for " + channel.getDisplayName());

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

        Log.d(TAG, "created dummy program info for channel: " + channel.getDisplayName());

        return dummy;
    }


    public static String[] getAllGenres() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return new String[]{
                    TvContract.Programs.Genres.ANIMAL_WILDLIFE,
                    TvContract.Programs.Genres.ARTS,
                    TvContract.Programs.Genres.COMEDY,
                    TvContract.Programs.Genres.DRAMA,
                    TvContract.Programs.Genres.EDUCATION,
                    TvContract.Programs.Genres.ENTERTAINMENT,
                    TvContract.Programs.Genres.FAMILY_KIDS,
                    TvContract.Programs.Genres.GAMING,
                    TvContract.Programs.Genres.LIFE_STYLE,
                    TvContract.Programs.Genres.MOVIES,
                    TvContract.Programs.Genres.MUSIC,
                    TvContract.Programs.Genres.NEWS,
                    TvContract.Programs.Genres.PREMIER,
                    TvContract.Programs.Genres.SHOPPING,
                    TvContract.Programs.Genres.SPORTS,
                    TvContract.Programs.Genres.TECH_SCIENCE,
                    TvContract.Programs.Genres.TRAVEL,
            };
        }
        return new String[]{
                TvContract.Programs.Genres.ANIMAL_WILDLIFE,
                TvContract.Programs.Genres.COMEDY,
                TvContract.Programs.Genres.DRAMA,
                TvContract.Programs.Genres.EDUCATION,
                TvContract.Programs.Genres.FAMILY_KIDS,
                TvContract.Programs.Genres.GAMING,
                TvContract.Programs.Genres.MOVIES,
                TvContract.Programs.Genres.NEWS,
                TvContract.Programs.Genres.SHOPPING,
                TvContract.Programs.Genres.SPORTS,
                TvContract.Programs.Genres.TRAVEL,
        };
    }

}
