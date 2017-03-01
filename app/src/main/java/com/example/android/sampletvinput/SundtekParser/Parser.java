package com.example.android.sampletvinput.SundtekParser;

import android.content.Context;
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
            "]"
            ;


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


    Context context;


    public Parser(Context context){ this.context = context; }


    public List<Channel> getChannels()  {
        List<Channel> channelList = new ArrayList<>();

        InternalProviderData internalProviderData = new InternalProviderData();
        internalProviderData.setRepeatable(true);
        JSONArray jsonResponse = null;
        JSONArray jsonChannel;

        String channelName = null;
        String channelDisplayNumber = null;
        String channelLogo = null;
        int originalNetworkId = 101;
        int serviceId = 0;

        Channel channel;

        try {
            jsonResponse = new JSONArray(getJson("http://192.168.3.1:22000/channels.json"));
//            Log.d("CHANNEL", jsonResponse.get(0).toString());


            for (int i = 0; i < 1; i++) {
                jsonChannel = jsonResponse.getJSONArray(i);
                channelName = jsonChannel.get(CHANNEL_NAME).toString();
                channelDisplayNumber = Integer.toString(i+1);
                channelLogo = jsonChannel.get(CHANNEL_LOGO).toString();

                String serviceIdString = jsonChannel.get(CHANNEL_ID).toString();
                serviceIdString = serviceIdString.substring(0, serviceIdString.indexOf("_"));
                serviceId = Integer.parseInt(serviceIdString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        channel = new Channel.Builder()
                .setDisplayName(channelName)
                .setDisplayNumber(channelDisplayNumber)
                .setChannelLogo(channelLogo)
                .setOriginalNetworkId(originalNetworkId)
                .setInternalProviderData(internalProviderData)
                .setServiceId(serviceId)
                .setPackageName(context.getPackageName())
                .build();

        channelList.add(channel);


        return channelList;
    }


    public List<Program> getPrograms(Channel channel){
        List<Program> programList = new ArrayList<>();

//        JSONArray jsonResponse = null;
//        JSONArray jsonChannel;
//
//        try {
//            jsonResponse = new JSONArray(getJson("http://192.168.3.1:22000/channels.json"));
////            Log.d("CHANNEL", jsonResponse.get(0).toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }




        JSONArray prog;
        String progTitle = null;
        String progSubtitle = null;
        String progIconSrc = null;
        int progId;
        long progStart = 0;
        long progEnd = 0;
        long progDuration = 0;
        String progStreamUrl = null;
        int progNum = 1;
//
//        long unixStart = 0;
//        long duration = 0;
//        long unixEnd = 0;

        InternalProviderData internalProviderData = new InternalProviderData();
        internalProviderData.setVideoType(Util.TYPE_OTHER);

        try {
            JSONArray channelJson = new JSONArray(one);


            for(int i = 0; i < channelJson.length(); i++){
                if(channelJson.get(i) instanceof  JSONArray) {
                    prog = new JSONArray(channelJson.get(i).toString());

                    progNum = progNum++;
                    progTitle = prog.get(PROG_TITLE).toString();
                    progSubtitle = prog.get(PROG_SUBTITLE).toString();
                    progId = (Integer) prog.get(PROG_ID);
                    progStart = prog.getLong(PROG_START);
                    progDuration = prog.getLong(PROG_DURATION);
                    progEnd = progStart + progDuration;

                    Log.d( "JSON PROGRAM",  "progNum: " + progNum);
                    Log.d( "JSON PROGRAM",  "progId: " + progId);
                    Log.d( "JSON PROGRAM" , "progTitle: " + progTitle );
                    Log.d( "JSON PROGRAM" , "progSubtitle: " + progSubtitle );
                    Log.d( "JSON PROGRAM" , "progStart: " + progStart );
                    Log.d( "JSON PROGRAM" , "progDuration: " + progDuration);
                    Log.d( "JSON PROGRAM" , "progEnd: " + progEnd );
                }else{
                    progIconSrc = channelJson.get(PROG_ICON_SRC).toString();
                    progStreamUrl = PROG_BASE_STREAM_URL + channelJson.get(1).toString().replace( " " , "_" );
                }
            }
            Log.d( "JSON CHANNEL" , "progStreamUrl: " + progStreamUrl );
            Log.d( "JSON CHANNEL" , "progIconSrc: " + progIconSrc );
        } catch (JSONException e) {
            e.printStackTrace();
        }



        internalProviderData.setVideoUrl(progStreamUrl);
        programList.add(new Program.Builder()
                .setTitle(progTitle)
                .setStartTimeUtcMillis(progStart)
                .setEndTimeUtcMillis(progEnd)
                .setDescription(progSubtitle)
//                .setCanonicalGenres(new String[] {TvContract.Programs.Genres.TECH_SCIENCE,
//                        TvContract.Programs.Genres.MOVIES})
//                .setPosterArtUri(progIconSrc)
//                .setThumbnailUri(progIconSrc)
                .setInternalProviderData(internalProviderData)
                .build());




//        try {
//            Log.d("JSON NAME" , prog.getString(3) + " " + prog.getString(4) + " -- " + startTime.toString() + " bis " +  endTime.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//




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
