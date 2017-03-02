package com.example.android.sampletvinput.JsonParser;

import android.content.Context;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Tamim Baschour on 26.02.2017.
 */

public class Parser {

    final static String CHANNELS = "[[\"MDR Sachsen\",\"http://192.168.3.1:22000/stream/MDR_Sachsen\",\"http://sundtek.de/picons/?p=MDR_Sachsen\",\"1\",\"28228_6e44\"],[\"NDR FS HH\",\"http://192.168.3.1:22000/stream/NDR_FS_HH\",\"http://sundtek.de/picons/?p=NDR_FS_HH\",\"1\",\"28225_6e41\"],[\"rbb Berlin\",\"http://192.168.3.1:22000/stream/rbb_Berlin\",\"http://sundtek.de/picons/?p=rbb_Berlin\",\"1\",\"28206_6e2e\"],[\"SWR Fernsehen BW\",\"http://192.168.3.1:22000/stream/SWR_Fernsehen_BW\",\"http://sundtek.de/picons/?p=SWR_Fernsehen_BW\",\"1\",\"28113_6dd1\"],[\"WDR Kln\",\"http://192.168.3.1:22000/stream/WDR_Kln\",\"http://sundtek.de/picons/?p=WDR_Koeln\",\"1\",\"28111_6dcf\"]]";

    final static String DETAILPROGRAM = "[\"http://sundtek.de/picons/?p=MDR_Sachsen\", \"MDR Sachsen\",\"28228\",[1488124800, 600, 34266,\"MDR aktuell\",\"mit Wetter\",\"In komprimierter Fassung von anderthalb Minuten informieren wir Sie &uuml;ber die aktuellen Nachrichten des Tages.<p>Produziert in HD\"]]";

//    final static String CHANNELPROGRAM = "[[\"http://sundtek.de/picons/?p=MDR_Sachsen\", \"MDR Sachsen\",\"28228\", \"1\",[1488113100, 11700, 34264,\"Sport im Osten\",\"Fu&szlig;ball 3. Liga live\",\"\"],[1488124800, 600, 34266,\"MDR aktuell\",\"mit Wetter\",\"\"],[1488125400, 3000, 34268,\"In aller Freundschaft - Die jungen &Auml;rzte (44)\",\"Neustart\",\"\"],[1488128400, 300, 34270,\"MDR aktuell\",\"\",\"\"],[1488128700, 2700, 34272,\"In aller Freundschaft (760)\",\"Ein schmaler Grat\",\"\"],[1488131400, 120, 34275,\"Wetter f&uuml;r 3\",\"Die Wetterschau f&uuml;r Mitteldeutschland\",\"\"],[1488131520, 480, 34277,\"Unser Sandm&auml;nnchen\",\"Pittiplatsch - Die hei&szlig;en Socken\",\"\"]]";


    final static String one =
            "[" +
                    "\"http://sundtek.de/picons/?p=MDR_Sachsen\"," +
                    "\"MDR Sachsen\"," +
                    "\"28228\"," +
                    "\"1\"," +
                    "[" +
                    "1488113100," +
                    "11700," +
                    "34264," +
                    "\"Sport im Osten\"," +
                    "\"Fu&szlig;ball 3. Liga live\"," +
                    "\"\"" +
                    "]," +
                    "[" +
                    "1488124800," +
                    "600," +
                    "34266," +
                    "\"MDR aktuell\"," +
                    "\"mit Wetter\"," +
                    "\"\"" +
                    "]" +
                    "]";


    final static int CHANNEL_NAME = 0;
    final static int CHANNEL_LOGO = 2;
    final static int CHANNEL_ID = 4;

    final static int PROG_START = 0;
    final static int PROG_ICON_SRC = 0;
    final static int PROG_DURATION = 1;
    final static int PROG_ID = 2;
    final static int PROG_TITLE = 3;
    final static int PROG_SUBTITLE = 4;
    final static String PROG_BASE_STREAM_URL = "http://192.168.3.1:22000/stream/";

    private JSONArray responseChannlesSdJson;
    private JSONArray responseChannlesHdJson;
    private JSONArray responseProgramsTodayJson;
    private JSONArray responseProgramsTomorrowJson;
    private int channelNumber = 1;


    Context context;

    public Parser() {
    }

    ;

    public Parser(Context context) {
        this.context = context;
    }


    public List<Channel> getChannels() throws JSONException {
        List<Channel> channelList = new ArrayList<>();

        if (responseChannlesHdJson == null)
            responseChannlesHdJson = new JSONArray(getJson("http://192.168.3.1:22000/servercmd.xhx?chantype=hdtv&filter=publictv-privatetv"));
        if (responseChannlesSdJson == null)
            responseChannlesSdJson = new JSONArray(getJson("http://192.168.3.1:22000/servercmd.xhx?chantype=sdtv&filter=publictv-privatetv"));

        channelList.addAll(parseChannles(responseChannlesHdJson));
        channelList.addAll(parseChannles(responseChannlesSdJson));

        return channelList;
    }

    public List<Channel> parseChannles(JSONArray channelsJson) throws JSONException {
        List<Channel> channelList = new ArrayList<>();

        InternalProviderData internalProviderData;
        JSONArray jsonChannel;

        String channelName;
        String channelLogo;
        int serviceId;

        Channel channel;

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


    public List<Program> getPrograms(Channel channel) throws JSONException {
        List<Program> programList = new ArrayList<>();

        responseProgramsTodayJson = new JSONArray(getJson("http://192.168.3.1:22000/servercmd.xhx?epgmode=today&epgfilter=today-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5"));
        responseProgramsTomorrowJson = new JSONArray(getJson("http://192.168.3.1:22000/servercmd.xhx?epgmode=today&epgfilter=today-publictv-privatetv&channels=1%2C2%2C3%2C4%2C5"));

        programList.addAll(parsePrograms(channel, responseProgramsTodayJson));
     //   programList.addAll(parsePrograms(channel, responseProgramsTomorrowJson));

        return programList;
    }


    public List<Program> parsePrograms(Channel channel, JSONArray programsJson) throws JSONException {

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
                    progStreamUrl = PROG_BASE_STREAM_URL + channelJson.get(1).toString().replace(" ", "_");
                    progChannelName = channelJson.get(1).toString();
                }
            }
        }

        return programList;
    }


    private String getJson(String jUrl) {
        HttpURLConnection connection;
        BufferedReader reader;

        try {
            URL url = new URL(jUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
