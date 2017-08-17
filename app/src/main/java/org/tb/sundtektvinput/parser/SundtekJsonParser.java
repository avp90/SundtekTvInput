package org.tb.sundtektvinput.parser;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer.util.Util;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.json.JSONArray;
import org.json.JSONException;
import org.tb.sundtektvinput.SundtekTvInputApp;
import org.tb.sundtektvinput.model.ProgramsDB;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import static android.media.tv.TvContract.Channels.TYPE_OTHER;

/**
 * Created by Tamim Baschour on 26.02.2017.
 */

public class SundtekJsonParser {

    private static final boolean DEBUG = false;

    private static final String TAG = SundtekJsonParser.class.getSimpleName();

    private final String BASE_URL;        // = "http://" + IP_ADDRESS + ":" + SERVER_PORT;
    private static final String BASE_SERVERCMD_URL = "/servercmd.xhx";
    private static final String SERVER_PORT = "22000";

    private static final String CHANNEL_LIST = "favs";


    private static final int CHANNEL_NAME = 0;
    private static final int CHANNEL_MEDIA = 1;
    private static final int CHANNEL_LOGO = 2;
    private static final int CHANNEL_ID = 4;

    public static final String CHANNEL_IPD_MEDIA_URL = "mediaUrl";
    public static final String CHANNEL_IPD_MEDIA_TYPE = "mediaType";

    private static final int PROG_START = 0;
    private static final int PROG_DURATION = 1;
    private static final int PROG_EVENTID = 2;
    private static final int PROG_TITLE = 3;
    private static final int PROG_SUBTITLE = 4;

    private static final int PROG_ICON_SRC = 0;
    private static final int PROG_CHANNEL_NAME = 1;
    private static final int PROG_SERVICE_ID = 2;

    private static final int PROG_DETAILS_DESCRIPTION = 5;

    private static final String PROG_IPD_EPG_EVENT_ID = "epgEventId";
    private static final String PROG_IPD_SERVICE_ID = "serviceId";
    private static final String PROG_IPD_ORIGINAL_NETWORK_ID = "originalNetworkId";
    private static final String PROG_IPD_CHANNEL_NAME = "channelName";

    private static final int JSON_EPG_START_INDEX = 3;

    private static final String CMD_PARAM = "cmd";
    private static final String CMD_GET_GROUPS = "getgroups";
    private static final String CMD_PROVIDERS = "providers";
    private static final String CMD_CHANNELLIST = "chlist";

    private static final String CHANNEL_MODE_PARAM = "mode";
    private static final String CHANNEL_MODE_SCANLIST = "scanlist";
    private static final String CHANNEL_MODE_FAVLIST = "favlist";

    private static final String DEVICE_SERIAL_PARAM = "devserial";

    private static final String PRID_PARAM = "prid";

    private static final String CHANNEL_FILTER_PARAM = "filter";
    private static final String CHANNEL_FILTER_RADIO_FTA = "radio";
    private static final String CHANNEL_FILTER_SD_FTA = "sd";
    private static final String CHANNEL_FILTER_HD_FTA = "hd";
    private static final String CHANNEL_FILTER_RADIO_PTV = "pradio";
    private static final String CHANNEL_FILTER_SD_PTV = "psd";
    private static final String CHANNEL_FILTER_HD_PTV = "phd";

    private static final String GROUPS_PARAM = "groups";

    private static final String EPG_MODE_PARAM = "epgmode";
    private static final String EPG_MODE_NOW = "now";
    private static final String EPG_MODE_TODAY = "today";
    private static final String EPG_MODE_TOMORROW = "tomorrow";
    private static final String EPG_MODE_CALENDAR = "calendar";

    private static final String EPG_FILTER_PARAM = "epgfilter";
    private static final String EPG_FILTER_NOW = "now";
    private static final String EPG_FILTER_TODAY = "today";
    private static final String EPG_FILTER_TOMORROW = "tomorrow";
    private static final String EPG_FILTER_GROUP = "group";


    private static final String EPG_START_MS_PARAM = "epgstart";
    private static final String EPG_STOP_MS_PARAM = "epgstop";
    private static final String EPG_DATE_PARAM = "date";


    private static final String EPG_SERVICE_ID_PARAM = "epgserviceid";
    private static final String EPG_EVENT_ID_PARAM = "epgeventid";
    private static final String EPG_DEL_SYS_PARAM = "delsys";

    private HashMap<String, Channel> channelMap;

    private int channelNumber = 1;


    public SundtekJsonParser(Context context) {
        String ip = new SettingsHelper(context).loadIp();
        String IP_ADDRESS;
        if (ip != null)
            IP_ADDRESS = ip;
        else
            IP_ADDRESS = "192.168.3.21";

        BASE_URL = "http://" + IP_ADDRESS + ":" + SERVER_PORT;
    }

    private HashMap<String, Channel> getChannelMap() throws JSONException, IOException {

        if (channelMap == null) {
            channelMap = new HashMap<>();
            if (DEBUG) {
                Log.d(TAG, "Fetch FAV channels");
            }
            JSONArray responseChannlesJson = new JSONArray(
                    getJson(
                            BASE_URL + BASE_SERVERCMD_URL,
                            buildChannelPostBody(
                                    CHANNEL_LIST,
                                    CHANNEL_MODE_FAVLIST,
                                    Arrays.asList(
                                            CHANNEL_FILTER_SD_FTA,
                                            CHANNEL_FILTER_HD_FTA,
                                            CHANNEL_FILTER_HD_PTV,
                                            CHANNEL_FILTER_SD_PTV,
                                            CHANNEL_FILTER_RADIO_FTA,
                                            CHANNEL_FILTER_RADIO_PTV
                                    )
                            )
                    )
            );
            channelMap.putAll(parseChannles(responseChannlesJson));

            if (DEBUG)
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

        channelsJson = (JSONArray) channelsJson.get(0);
        for (int i = 0; i < channelsJson.length(); i++) {
            if (channelsJson.get(i) instanceof JSONArray) { // filter out non channel data
                jsonChannel = channelsJson.getJSONArray(i);
                channelName = jsonChannel.get(CHANNEL_NAME).toString();
                channelLogo = jsonChannel.get(CHANNEL_LOGO).toString();
                mediaUrl = jsonChannel.get(CHANNEL_MEDIA).toString();
                originalNetworkId = channelName.hashCode();

                String serviceIdString = jsonChannel.get(CHANNEL_ID).toString();
                serviceIdString = serviceIdString.substring(0, serviceIdString.indexOf("_"));
                serviceId = Integer.parseInt(serviceIdString);

                internalProviderData = new InternalProviderData();
                internalProviderData.setRepeatable(false);
                internalProviderData.put(CHANNEL_IPD_MEDIA_URL, mediaUrl);
                internalProviderData.put(CHANNEL_IPD_MEDIA_TYPE, Util.TYPE_OTHER);


                channels.put(String.valueOf(originalNetworkId), new Channel.Builder()
                                .setDisplayName(channelName)
                                .setDisplayNumber(String.valueOf(channelNumber++))
//                                .setChannelLogo(channelLogo)
                                .setOriginalNetworkId(originalNetworkId)
                                .setInternalProviderData(internalProviderData)
                                .setServiceId(serviceId)
                                .setType(TYPE_OTHER)
                                .build()
                );
            }
        }

        return channels;
    }


    public List<Program> getPrograms() {
        List<Program> programList = new ArrayList<>();

        try {
            if (DEBUG)
                Log.d(TAG, "Fetch programs");

            JSONArray responseProgramsNowJson = new JSONArray(
                    getJson(
                            BASE_URL + BASE_SERVERCMD_URL,
                            buildProgramsPostBody(
                                    CHANNEL_LIST,
                                    EPG_MODE_TODAY,
                                    Arrays.asList(
                                            "now",
                                            "group"
                                    )
                            )
                    )
            );
            programList = parsePrograms(responseProgramsNowJson, true);

            if (DEBUG) {
                Log.d(TAG, "Found " + programList.size() + " programs");
                Log.d(TAG, programList.toString());
            }

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
        int originalNetworkId;
        long start;
        long end;
        long duration;
        String epgEventId;
        String serviceId;
        String description = "";
        String channelName;
        int genreIndex = 0;

        InternalProviderData internalProviderData;

        for (int resp = 0; resp < programsJson.length(); resp++) {
            JSONArray channelProgramsJson = programsJson.getJSONArray(resp);

            serviceId = channelProgramsJson.getString(PROG_SERVICE_ID);
            channelName = channelProgramsJson.get(PROG_CHANNEL_NAME).toString();
            originalNetworkId = channelName.hashCode();

            internalProviderData = new InternalProviderData();
            internalProviderData.put(PROG_IPD_SERVICE_ID, serviceId);
            internalProviderData.put(PROG_IPD_ORIGINAL_NETWORK_ID, originalNetworkId);
            //set originalNetworkId as fake eventId in case there are no real events for the channel
            internalProviderData.put(PROG_IPD_EPG_EVENT_ID, originalNetworkId);
            internalProviderData.put(PROG_IPD_CHANNEL_NAME, channelName);

            if (channelProgramsJson.length() > (JSON_EPG_START_INDEX + 1)) {
                for (int i = JSON_EPG_START_INDEX; i < channelProgramsJson.length(); i++) {
                    if (channelProgramsJson.get(i) instanceof JSONArray) {
                        prog = new JSONArray(channelProgramsJson.get(i).toString());
                        title = prog.get(PROG_TITLE).toString();
                        subtitle = prog.get(PROG_SUBTITLE).toString();

                        title = subtitle.isEmpty() ? title : title + " - " + subtitle;
                        start = (prog.getLong(PROG_START) * 1000L);
                        duration = (prog.getLong(PROG_DURATION) * 1000L);
                        end = start + duration;
                        epgEventId = prog.getString(PROG_EVENTID);
                        internalProviderData.put(PROG_IPD_EPG_EVENT_ID, epgEventId);
                        if (parseDescription)
                            description = getJsonDescription(serviceId, epgEventId);
                        //TODO:REMOVE FAKE GENRES
                        String[] genre = new String[]{ProgramsDB.getAllGenres()[(genreIndex++) % ProgramsDB.getAllGenres().length]};

                        if (!(end == start || end < start))
                            programList.add(new Program.Builder()
                                    .setTitle(title)
                                    .setStartTimeUtcMillis(start)
                                    .setEndTimeUtcMillis(end)
                                    .setInternalProviderData(internalProviderData)
                                    .setDescription(description.length() > 256 ? description.substring(0, 255) : description)
                                    .setLongDescription(description)
                                    .setCanonicalGenres(genre)
//                                    .setPosterArtUri(logo)
//                                    .setThumbnailUri(logo)
                                    .build());
                    }
                }
            }
        }

        return programList;
    }


    private String getJsonDescription(String serviceId, String epgEventId) {
        if (!epgEventId.isEmpty() && !serviceId.isEmpty())
            try {
                JSONArray detailsJson = new JSONArray(
                        getJson(
                                BASE_URL + BASE_SERVERCMD_URL,
                                buildDescriptionPostBody(
                                        serviceId,
                                        epgEventId,
                                        "1"
                                )
                        )
                );

                for (int i = 0; i < detailsJson.length(); i++) {
                    if (!detailsJson.isNull(i) && (detailsJson.get(i) instanceof JSONArray)) {
                        JSONArray detailsArray = detailsJson.getJSONArray(i);
                        if (!detailsArray.isNull(PROG_DETAILS_DESCRIPTION) && !detailsArray.getString(PROG_DETAILS_DESCRIPTION).isEmpty()) {
                            if (DEBUG)
                                Log.d(TAG, "added Description for event: " + epgEventId);
                            return detailsArray.getString(PROG_DETAILS_DESCRIPTION);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        return "";
    }


    private RequestBody buildChannelPostBody(String groups, String channelMode, List<String> channelFilters) {
        return new FormBody.Builder()
                .add(CMD_PARAM, CMD_CHANNELLIST)
                .add(CHANNEL_MODE_PARAM, channelMode)
                .add(GROUPS_PARAM, "[" + (groups.isEmpty() ? groups : "\"" + groups + "\"") + "]")
                .add(CHANNEL_FILTER_PARAM, TextUtils.join("-", channelFilters))
                .build();
    }


    private RequestBody buildProgramsPostBody(String groups, String epgMode, List<String> epgFilter) {
        return new FormBody.Builder()
                .add(EPG_MODE_PARAM, epgMode)
                .add(EPG_FILTER_PARAM, TextUtils.join("-", epgFilter))
                .add(GROUPS_PARAM, "[" + (groups.isEmpty() ? groups : "\"" + groups + "\"") + "]")
                .build();
    }

    private RequestBody buildDescriptionPostBody(String serviceId, String eventId, String delsys) {
        return new FormBody.Builder()
                .add(EPG_SERVICE_ID_PARAM, serviceId)
                .add(EPG_EVENT_ID_PARAM, eventId)
                .add(EPG_DEL_SYS_PARAM, delsys)
                .build();
    }

    private static String getJson(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = SundtekTvInputApp.httpClient.newCall(request).execute();

        return response.body().string();
    }
}

