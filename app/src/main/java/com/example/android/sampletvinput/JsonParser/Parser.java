package com.example.android.sampletvinput.JsonParser;

import android.media.tv.TvContract;
import android.util.Log;

import com.google.android.exoplayer.util.Util;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Tamim Baschour on 26.02.2017.
 */

public class Parser {


    private static final String TAG = Parser.class.getSimpleName();

    private static final int CHANNEL_NAME = 0;
    private static final int CHANNEL_LOGO = 2;
    private static final int CHANNEL_ID = 4;

    private static final int PROG_START = 0;
    private static final int PROG_DURATION = 1;
    private static final int PROG_EVENTID = 2;
    private static final int PROG_TITLE = 3;
    private static final int PROG_SUBTITLE = 4;

    private static final int PROG_ICON_SRC = 0;
    private static final int PROG_CHANNEL_NAME = 1;
    private static final int PROG_SERVICE_ID = 2;


    private static final String BASE_URL = "http://192.168.3.1:22000";
    private static final String BASE_STREAM_URL = BASE_URL + "/stream/";
    private static final String BASE_SERVERCMD_URL = BASE_URL + "/servercmd.xhx?";
    private static final String QUERY_CHANNELS_SD = "chantype=sdtv&filter=publictv-privatetv";
    private static final String QUERY_CHANNELS_HD = "chantype=hdtv&filter=publictv-privatetv";
    private static final String QUERY_PROGRAMS_NOW = "epgmode=now&epgfilter=now-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";
    private static final String QUERY_PROGRAMS_TODAY = "epgmode=today&epgfilter=today-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";
    private static final String QUERY_PROGRAMS_TOMORROW = "epgmode=tomorrow&epgfilter=tomorrow-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";

    private static final String PROG_IPD_EPG_EVENT_ID = "epgEventId";
    private static final String PROG_IPD_SERVICE_ID = "serviceId";
    private static final String PROG_IPD_ORIGINAL_NETWORK_ID = "originalNetworkId";
    private static final String PROG_IPD_CHANNEL = "channel";

    private JSONArray responseChannlesSdJson;
    private JSONArray responseChannlesHdJson;
    private JSONArray responseProgramsNowJson;
    private JSONArray responseProgramsTodayJson;
    private JSONArray responseProgramsTomorrowJson;

    private int channelNumber = 1;

    public Parser() {
        Log.d(TAG, "new Parser");
    }

    public List<Channel> getChannels() throws JSONException, IOException {
        List<Channel> channelList = new ArrayList<>();
        List<Channel> channelsHd;
        List<Channel> channelsSd;


        if (responseChannlesHdJson == null) {
            Log.d(TAG, "responseChannlesHdJson == null");
            responseChannlesHdJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_CHANNELS_HD));
        }
        channelsHd = parseChannles(responseChannlesHdJson);
        Log.d(TAG, "HD channeles found: " + channelsHd.size());

        if (responseChannlesSdJson == null) {
            Log.d(TAG, "responseChannlesSdJson == null");
            responseChannlesSdJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_CHANNELS_SD));
        }
        channelsSd = parseChannles(responseChannlesSdJson);
        Log.d(TAG, "SD channels found: " + channelsSd.size());

        channelList.addAll(channelsHd);
        channelList.addAll(channelsSd);

        Log.d(TAG, "total channels found: " + channelList.size());
        return channelList;
    }

    private List<Channel> parseChannles(JSONArray channelsJson) throws JSONException {
        List<Channel> channelList = new ArrayList<>();

        InternalProviderData internalProviderData;
        JSONArray jsonChannel;

        String channelName;
        String channelLogo;
        int serviceId;

        for (int i = 0; i < channelsJson.length(); i++) {
            jsonChannel = channelsJson.getJSONArray(i);
            channelName = jsonChannel.get(CHANNEL_NAME).toString();
            channelLogo = jsonChannel.get(CHANNEL_LOGO).toString();

            String serviceIdString = jsonChannel.get(CHANNEL_ID).toString();
            serviceIdString = serviceIdString.substring(0, serviceIdString.indexOf("_"));
            serviceId = Integer.parseInt(serviceIdString);

            internalProviderData = new InternalProviderData();
            internalProviderData.setRepeatable(false);

            channelList.add(new Channel.Builder()
                    .setDisplayName(channelName)
                    .setDisplayNumber(String.valueOf(channelNumber++))
                    .setChannelLogo(channelLogo)
                    .setOriginalNetworkId(channelLogo.hashCode())
                    .setInternalProviderData(internalProviderData)
                    .setServiceId(serviceId)
                    .build()
            );
        }

        return channelList;
    }


    public List<Program> getPrograms(Channel channel, Boolean refresh) {
        List<Program> programList = new ArrayList<>();
        List<Program> programNow = new ArrayList<>();
        List<Program> programToday = new ArrayList<>();
        List<Program> programTomorrow = new ArrayList<>();

        try {
//            if ((responseProgramsNowJson == null) || refresh)
//                responseProgramsNowJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_PROGRAMS_NOW));
//            programNow = parsePrograms(channel, responseProgramsNowJson);
//            Log.d(TAG, "finished fetching programs for Now (Size: " + programNow.size() + ")");
            if ((responseProgramsTodayJson == null) || refresh)
                responseProgramsTodayJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_PROGRAMS_TODAY));
            programToday = parsePrograms(channel, responseProgramsTodayJson);
            Log.d(TAG, channel.getDisplayName() + " programs today: " + programToday.size());

            if ((responseProgramsTomorrowJson == null) || refresh)
                responseProgramsTomorrowJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_PROGRAMS_TOMORROW));
            programTomorrow = parsePrograms(channel, responseProgramsTomorrowJson);
            Log.d(TAG, channel.getDisplayName() + " programs tomorrow: " + programTomorrow.size());

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        programList.addAll(programNow);
        programList.addAll(programToday);
        programList.addAll(programTomorrow);

        Log.d(TAG, channel.getDisplayName() + " total programs found: " + programList.size());

        return programList;
    }


    private List<Program> parsePrograms(Channel channel, JSONArray programsJson) throws
            JSONException {

        List<Program> programList = new ArrayList<>();
        long currentTime = new Date().getTime();

        JSONArray prog;
        String title;
        String subtitle;
        String description = "no Description";
        String logo = "";
        int originalNetworkId = -1;
        long start;
        long end;
        long duration;
        String streamUrl = "";
        String epgEventId;
        int serviceId = 0;

        InternalProviderData internalProviderData;

        for (int resp = 0; resp < programsJson.length(); resp++) {
            JSONArray channelJson = programsJson.getJSONArray(resp);

            for (int i = 0; i < channelJson.length(); i++) {
                if (channelJson.get(i) instanceof JSONArray) {
                    if (channel.getOriginalNetworkId() == originalNetworkId) {
                        prog = new JSONArray(channelJson.get(i).toString());
                        title = prog.get(PROG_TITLE).toString();
                        subtitle = prog.get(PROG_SUBTITLE).toString();

                        title = subtitle.equals("") ? title : title + " - " + subtitle;
                        start = (prog.getLong(PROG_START) * 1000L);
                        duration = (prog.getLong(PROG_DURATION) * 1000L);
                        end = start + duration;
                        epgEventId = prog.getString(PROG_EVENTID);

                        internalProviderData = new InternalProviderData();
                        internalProviderData.setVideoUrl(streamUrl);
                        internalProviderData.setVideoType(Util.TYPE_OTHER);
                        internalProviderData.put(PROG_IPD_EPG_EVENT_ID, epgEventId);
                        internalProviderData.put(PROG_IPD_SERVICE_ID, serviceId);
                        internalProviderData.put(PROG_IPD_ORIGINAL_NETWORK_ID, logo.hashCode());
                        internalProviderData.put(PROG_IPD_CHANNEL, channel);


//                        if (((start - currentTime) > 0) && ((start - currentTime) < 7200000)) {
//                            Log.d("TIMEDIST", "start: " + start + " current: " + currentTime + " diff: " + (start - currentTime) + " title: " + title + " channel: " + channel.getDisplayName());
//                            if (!epgEventId.equals("") && (serviceId != 0)) {
//                                description = getJsonDescription(serviceId, epgEventId);
//                            }
//                        }

                        programList.add(new Program.Builder()
                                .setTitle(title)
                                .setStartTimeUtcMillis(start)
                                .setEndTimeUtcMillis(end)
                                .setThumbnailUri(logo)
                                .setInternalProviderData(internalProviderData)
//                                .setDescription(description.length() > 256 ? description.substring(0, 255) : description)
//                                .setLongDescription(description)
                                .setCanonicalGenres(new String[]{TvContract.Programs.Genres.ENTERTAINMENT,
                                        TvContract.Programs.Genres.MOVIES, TvContract.Programs.Genres.TECH_SCIENCE})
                                .setPosterArtUri(logo)
                                .setThumbnailUri(logo)
                                .build());
                    }
                } else {
                    logo = channelJson.get(PROG_ICON_SRC).toString();
                    originalNetworkId = logo.hashCode();
                    streamUrl = BASE_STREAM_URL + channelJson.get(PROG_CHANNEL_NAME).toString().replace(" ", "_");
                    serviceId = channelJson.getInt(PROG_SERVICE_ID);
                }
            }
        }
        return programList;
    }

    private String getJsonDescription(int serviceId, String epgEventId) {

        try {
            JSONArray detailsJson = new JSONArray(getJson(BASE_SERVERCMD_URL + "epgserviceid=" + serviceId + "&epgeventid=" + epgEventId + "&delsys=1"));

            for (int k = 0; k < detailsJson.length(); k++) {
                if (!detailsJson.isNull(k) && (detailsJson.get(k) instanceof JSONArray)) {
                    JSONArray detailsArray = detailsJson.getJSONArray(k);
                    if (!detailsArray.isNull(5) && !detailsArray.get(5).equals("")) {
                        return detailsArray.getString(5);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";
    }


    private String getJson(String jUrl) throws IOException {
        HttpURLConnection connection;
        BufferedReader reader;


        URL url = new URL(jUrl);
        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(300000);
        connection.setReadTimeout(300000);
        connection.connect();


        InputStream stream = connection.getInputStream();

        reader = new BufferedReader(new InputStreamReader(stream));

        StringBuilder buffer = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            buffer.append(line).append("\n");
        }

        return buffer.toString();
    }
}
