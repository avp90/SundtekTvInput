package com.example.android.sampletvinput.JsonParser;

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
import java.util.List;


/**
 * Created by Tamim Baschour on 26.02.2017.
 */

public class Parser {

    private final static int CHANNEL_NAME = 0;
    private final static int CHANNEL_LOGO = 2;
    private final static int CHANNEL_ID = 4;

    private final static int PROG_START = 0;
    private final static int PROG_ICON_SRC = 0;
    private final static int PROG_DURATION = 1;
    private final static int PROG_TITLE = 3;
    private final static int PROG_SUBTITLE = 4;

    private final static String BASE_URL = "http://192.168.3.1:22000";
    private final static String BASE_STREAM_URL = BASE_URL + "/stream/";
    private final static String BASE_SERVERCMD_URL = BASE_URL + "/servercmd.xhx?";
    private final static String QUERY_CHANNELS_SD = "chantype=sdtv&filter=publictv-privatetv";
    private final static String QUERY_CHANNELS_HD = "chantype=hdtv&filter=publictv-privatetv";
    private final static String QUERY_PROGRAMS_NOW = "epgmode=now&epgfilter=now-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";
    private final static String QUERY_PROGRAMS_TODAY = "epgmode=today&epgfilter=today-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";
    private final static String QUERY_PROGRAMS_TOMORROW = "epgmode=tomorrow&epgfilter=tomorrow-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";


    private static JSONArray responseChannlesSdJson;
    private static JSONArray responseChannlesHdJson;
    private static JSONArray responseProgramsNowJson;
    private static JSONArray responseProgramsTodayJson;
    private static JSONArray responseProgramsTomorrowJson;

    private int channelNumber = 1;

    public List<Channel> getChannels() throws JSONException, IOException {
        List<Channel> channelList = new ArrayList<>();

        if (responseChannlesHdJson == null)
            responseChannlesHdJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_CHANNELS_HD));
        if (responseChannlesSdJson == null)
            responseChannlesSdJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_CHANNELS_SD));

        channelList.addAll(parseChannles(responseChannlesHdJson));
        channelList.addAll(parseChannles(responseChannlesSdJson));

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
                    .setOriginalNetworkId(serviceId)
                    .setInternalProviderData(internalProviderData)
                    .setServiceId(serviceId)
                    .build()
            );
        }

        return channelList;
    }


    public List<Program> getPrograms(Channel channel) throws JSONException, IOException {
        List<Program> programList = new ArrayList<>();

//        if (responseProgramsNowJson == null)
//            responseProgramsNowJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_PROGRAMS_NOW));
        if (responseProgramsTodayJson == null)
            responseProgramsTodayJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_PROGRAMS_TODAY));
        if (responseProgramsTomorrowJson == null)
            responseProgramsTomorrowJson = new JSONArray(getJson(BASE_SERVERCMD_URL + QUERY_PROGRAMS_TOMORROW));

//        programList.addAll(parsePrograms(channel, responseProgramsNowJson));
        programList.addAll(parsePrograms(channel, responseProgramsTodayJson));
        programList.addAll(parsePrograms(channel, responseProgramsTomorrowJson));

        return programList;
    }


    private List<Program> parsePrograms(Channel channel, JSONArray programsJson) throws JSONException {

        List<Program> programList = new ArrayList<>();

        JSONArray prog;
        String progTitle;
        String progSubtitle;
        String progIconSrc = null;
        long progStart;
        long progEnd;
        long progDuration;
        String progStreamUrl = null;
        String progChannelName = null;

        InternalProviderData internalProviderData;

        for (int resp = 0; resp < programsJson.length(); resp++) {
            JSONArray channelJson = programsJson.getJSONArray(resp);

            for (int i = 0; i < channelJson.length(); i++) {
                if (channelJson.get(i) instanceof JSONArray) {

                    assert progChannelName != null;
                    if (progChannelName.equals(channel.getDisplayName())) {
                        prog = new JSONArray(channelJson.get(i).toString());
                        progTitle = prog.get(PROG_TITLE).toString();
                        progSubtitle = prog.get(PROG_SUBTITLE).toString();
                        progStart = (prog.getLong(PROG_START) * 1000);
                        progDuration = (prog.getLong(PROG_DURATION) * 1000);
                        progEnd = progStart + progDuration;

                        internalProviderData = new InternalProviderData();
                        internalProviderData.setVideoUrl(progStreamUrl);
                        internalProviderData.setVideoType(Util.TYPE_OTHER);
                        programList.add(new Program.Builder()
                                .setTitle(progTitle + " - " + progSubtitle)
                                .setStartTimeUtcMillis(progStart)
                                .setEndTimeUtcMillis(progEnd)
                                .setThumbnailUri(progIconSrc)
                                .setInternalProviderData(internalProviderData)
                                .build());
                    }
                } else {
                    progIconSrc = channelJson.get(PROG_ICON_SRC).toString();
                    progStreamUrl = BASE_STREAM_URL + channelJson.get(1).toString().replace(" ", "_");
                    progChannelName = channelJson.get(1).toString();
                }
            }
        }

        return programList;
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
