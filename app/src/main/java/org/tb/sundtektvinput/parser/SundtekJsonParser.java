package org.tb.sundtektvinput.parser;

import android.content.Context;
import android.media.tv.TvContract;
import android.util.Log;

import com.google.android.exoplayer.util.Util;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.json.JSONArray;
import org.json.JSONException;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Tamim Baschour on 26.02.2017.
 */

public class SundtekJsonParser {


    private static final String TAG = SundtekJsonParser.class.getSimpleName();

    private static final int CHANNEL_NAME = 0;
    private static final int CHANNEL_MEDIA = 1;
    private static final int CHANNEL_LOGO = 2;
    private static final int CHANNEL_ID = 4;

    private static final String CHANNEL_IPD_MEDIA_URL = "mediaUrl";


    private static final int PROG_START = 0;
    private static final int PROG_DURATION = 1;
    private static final int PROG_EVENTID = 2;
    private static final int PROG_TITLE = 3;
    private static final int PROG_SUBTITLE = 4;

    private static final int PROG_ICON_SRC = 0;
    private static final int PROG_CHANNEL_NAME = 1;
    private static final int PROG_SERVICE_ID = 2;


    private String IP_ADDRESS;
    private String BASE_URL;
    private static final String BASE_STREAM_URL = "/stream/";
    private static final String BASE_SERVERCMD_URL = "/servercmd.xhx?";
    private static final String QUERY_CHANNELS_SD = "chantype=sdtv&filter=publictv-privatetv";
    private static final String QUERY_CHANNELS_HD = "chantype=hdtv&filter=publictv-privatetv";
    private static final String QUERY_PROGRAMS_NOW = "epgmode=now&epgfilter=now-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";
    private static final String QUERY_PROGRAMS_TODAY = "epgmode=today&epgfilter=today-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";
    private static final String QUERY_PROGRAMS_TOMORROW = "epgmode=tomorrow&epgfilter=tomorrow-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5";

    private static final String PROG_IPD_EPG_EVENT_ID = "epgEventId";
    private static final String PROG_IPD_SERVICE_ID = "serviceId";
    private static final String PROG_IPD_ORIGINAL_NETWORK_ID = "originalNetworkId";
    private static final String PROG_IPD_CHANNEL_NAME = "channelName";

    private static final int JSON_EPG_START_INDEX = 3;

    private HashMap<String, Channel> channelMap;
    private int channelNumber = 1;

    private Context mContext;


    public SundtekJsonParser(){}

    public SundtekJsonParser(Context context){
        mContext = context;
        String ip = new SettingsHelper(context).loadIp();
        if(ip != null)
            IP_ADDRESS = ip;
        else
            IP_ADDRESS = "192.168.3.26";

        BASE_URL = "http://" + IP_ADDRESS + ":22000";



    }

    public HashMap<String, Channel> getChannelMap() throws JSONException, IOException {

        if (channelMap == null) {
            channelMap = new HashMap<>();

            Log.d(TAG, "Fetch HD channels");
            JSONArray responseChannlesHdJson = new JSONArray(getJson(BASE_URL + BASE_SERVERCMD_URL + QUERY_CHANNELS_HD));
            channelMap.putAll(parseChannles(responseChannlesHdJson));

            Log.d(TAG, "Fetch SD channels");
            JSONArray responseChannlesSdJson = new JSONArray(getJson(BASE_URL + BASE_SERVERCMD_URL + QUERY_CHANNELS_SD));
            channelMap.putAll(parseChannles(responseChannlesSdJson));


            Log.d(TAG, "total channels found: " + channelMap.size());
        }
        return channelMap;
    }

    public ArrayList<Channel> getChannels() {
        try {
            return new ArrayList<>(getChannelMap().values());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private Map<String, Channel> parseChannles(JSONArray channelsJson) throws JSONException {

        HashMap<String, Channel> channels = new HashMap<>();

        InternalProviderData internalProviderData;
        JSONArray jsonChannel;

        String channelName;
        String channelLogo;
        String mediaUrl;
        int originalNetworkId;
        int serviceId;


        for (int i = 0; i < channelsJson.length(); i++) {
            jsonChannel = channelsJson.getJSONArray(i);
            channelName = jsonChannel.get(CHANNEL_NAME).toString();
            channelLogo = jsonChannel.get(CHANNEL_LOGO).toString();
            mediaUrl = jsonChannel.get(CHANNEL_MEDIA).toString();
            originalNetworkId = channelLogo.hashCode();

            String serviceIdString = jsonChannel.get(CHANNEL_ID).toString();
            serviceIdString = serviceIdString.substring(0, serviceIdString.indexOf("_"));
            serviceId = Integer.parseInt(serviceIdString);

            internalProviderData = new InternalProviderData();
            internalProviderData.setRepeatable(false);
            internalProviderData.put(CHANNEL_IPD_MEDIA_URL, mediaUrl);


            channels.put(String.valueOf(originalNetworkId), new Channel.Builder()
                    .setDisplayName(channelName)
                    .setDisplayNumber(String.valueOf(channelNumber++))
                    .setChannelLogo(channelLogo)
                    .setOriginalNetworkId(originalNetworkId)
                    .setInternalProviderData(internalProviderData)
                    .setServiceId(serviceId)
                    .build()
            );
        }

        return channels;
    }


    public List<Program> getPrograms() {
        List<Program> programList = new ArrayList<>();
        List<Program> programNow = new ArrayList<>();
        List<Program> programToday = new ArrayList<>();
        List<Program> programTomorrow = new ArrayList<>();

        try {
            Log.d(TAG, "Fetch programs for NOW");
            JSONArray responseProgramsNowJson = new JSONArray(getJson(BASE_URL + BASE_SERVERCMD_URL + QUERY_PROGRAMS_NOW));
            programNow = parsePrograms(responseProgramsNowJson, false);
            programList.addAll(programNow);
            Log.d(TAG, "Found " + programNow.size() + " programs for NOW");
//
//            Log.d(TAG, "Fetch programs for TODAY");
//            JSONArray responseProgramsTodayJson = new JSONArray(getJson(BASE_URL + BASE_SERVERCMD_URL + QUERY_PROGRAMS_TODAY));
//            programToday = parsePrograms(responseProgramsTodayJson, false);
//            programList.addAll(programToday);
//            Log.d(TAG, "Found " + programToday.size() + " programs for TODAY");
//
//            Log.d(TAG, "Fetch programs for TOMORROW");
//            JSONArray responseProgramsTomorrowJson = new JSONArray(getJson(BASE_URL + BASE_SERVERCMD_URL + QUERY_PROGRAMS_TOMORROW));
//            programTomorrow = parsePrograms(responseProgramsTomorrowJson, false);
//            programList.addAll(programTomorrow);
//            Log.d(TAG, "Found " + programTomorrow.size() + " programs for TOMORROW");

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return programList;
    }


    private List<Program> parsePrograms(JSONArray programsJson, Boolean parseDescription) throws
            JSONException {

        List<Program> programList = new ArrayList<>();

        JSONArray prog;
        String title;
        String subtitle;
        String logo;
        int originalNetworkId;
        long start;
        long end;
        long duration;
        String mediaUrl = "empty";
        String epgEventId;
        String serviceId;
        String description = "";
        String channelName;

        InternalProviderData internalProviderData;

        for (int resp = 0; resp < programsJson.length(); resp++) {
            JSONArray channelJson = programsJson.getJSONArray(resp);

            logo = channelJson.get(PROG_ICON_SRC).toString();
            originalNetworkId = logo.hashCode();
            serviceId = channelJson.getString(PROG_SERVICE_ID);
            channelName = channelJson.get(PROG_CHANNEL_NAME).toString();
            try {
                mediaUrl = (String) getChannelMap().get(String.valueOf(originalNetworkId)).getInternalProviderData().get(CHANNEL_IPD_MEDIA_URL);
            } catch (IOException e) {
                e.printStackTrace();
            }

            internalProviderData = new InternalProviderData();
            internalProviderData.setVideoUrl(mediaUrl);
            internalProviderData.setVideoType(Util.TYPE_OTHER);
            internalProviderData.put(PROG_IPD_SERVICE_ID, serviceId);
            internalProviderData.put(PROG_IPD_ORIGINAL_NETWORK_ID, originalNetworkId);
            //set originalNetworkId as fake eventId in case there are no real events for the channel
            internalProviderData.put(PROG_IPD_EPG_EVENT_ID, originalNetworkId);
            internalProviderData.put(PROG_IPD_CHANNEL_NAME, channelName);

            if (channelJson.length() > (JSON_EPG_START_INDEX + 1)) {
                for (int i = JSON_EPG_START_INDEX; i < channelJson.length(); i++) {
                    if (channelJson.get(i) instanceof JSONArray) {
                        prog = new JSONArray(channelJson.get(i).toString());
                        title = prog.get(PROG_TITLE).toString();
                        subtitle = prog.get(PROG_SUBTITLE).toString();

                        title = subtitle.equals("") ? title : title + " - " + subtitle;
                        start = (prog.getLong(PROG_START) * 1000L);
                        duration = (prog.getLong(PROG_DURATION) * 1000L);
                        end = start + duration;
                        epgEventId = prog.getString(PROG_EVENTID);
                        internalProviderData.put(PROG_IPD_EPG_EVENT_ID, epgEventId);
                        if (parseDescription && !epgEventId.equals("") && !serviceId.equals("empty"))
                            description = getJsonDescription(serviceId, epgEventId);


                        if (!(end == start || end < start))
                            programList.add(new Program.Builder()
                                    .setTitle(title)
                                    .setStartTimeUtcMillis(start)
                                    .setEndTimeUtcMillis(end)
                                    .setInternalProviderData(internalProviderData)
                                    .setDescription(description.length() > 256 ? description.substring(0, 255) : description)
                                    .setLongDescription(description)
                                    .setCanonicalGenres(new String[]{TvContract.Programs.Genres.ENTERTAINMENT,
                                            TvContract.Programs.Genres.MOVIES, TvContract.Programs.Genres.TECH_SCIENCE})
//                                .setPosterArtUri(logo)
                                    .setThumbnailUri(logo)
                                    .build());
                    }
                }
            }
        }

        return programList;
    }


    private String getJsonDescription(String serviceId, String epgEventId) {
        if (!epgEventId.equals("") && (!serviceId.equals("")))
            try {
                JSONArray detailsJson = new JSONArray(getJson(BASE_URL + BASE_SERVERCMD_URL + "epgserviceid=" + serviceId + "&epgeventid=" + epgEventId + "&delsys=1"));

                for (int k = 0; k < detailsJson.length(); k++) {
                    if (!detailsJson.isNull(k) && (detailsJson.get(k) instanceof JSONArray)) {
                        JSONArray detailsArray = detailsJson.getJSONArray(k);
                        if (!detailsArray.isNull(5) && !detailsArray.get(5).equals("")) {
                            Log.d(TAG, "added Description for event: " + epgEventId);
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
