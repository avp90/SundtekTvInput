package com.example.android.sampletvinput.SundtekParser;

import android.content.Context;
import android.util.Log;

import com.example.android.sampletvinput.SundtekParser.Model.SundtekProgram;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.gson.GsonBuilder;

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


    Context context;


    public Parser(Context context){ this.context = context; }


    public Object test(){
        GsonBuilder builder = new GsonBuilder();
        Object o = builder.create().fromJson(CHANNELS, Object.class);

        return o;
    }

    public List<Channel> getChannels()  {
        List<Channel> channelList = new ArrayList<>();

        InternalProviderData internalProviderData = new InternalProviderData();

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

            for (int i = 0; i < jsonResponse.length(); i++) {
                jsonChannel = jsonResponse.getJSONArray(i);
                channelName = jsonChannel.getString(CHANNEL_NAME);
                channelDisplayNumber = Integer.toString(i);
                channelLogo = jsonChannel.getString(CHANNEL_LOGO);

                String serviceIdString = jsonChannel.getString(CHANNEL_ID);
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
//                            .setPackageName(context.getPackageName())
                .build();

        channelList.add(channel);

        Log.d("CHANNEL", channel.toString());

//        for ( int j = 0; j < jArray.getJSONArray(i).length(); j++){
//            Log.d("JSON" ,  jArray.getJSONArray(i).get(j).toString());
//        }



//        for (Channel ch : channelList)
//            Log.d("CHANNEL", ch.toString());

        return channelList;
    }

    private String getJson(String surl) {
        HttpURLConnection connection;
        BufferedReader reader;

        try {
            URL url = new URL(surl);
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

    public List<SundtekProgram> getPrograms(){
        List<SundtekProgram> ProgramList = new ArrayList<>();

        JSONArray prog = null;
        java.util.Date endTime;
        java.util.Date startTime;
        long unixStart = 0;
        long duration = 0;
        long unixEnd = 0;
        try {
            JSONArray all = new JSONArray(one);

            for(int i = 0; i < all.length(); i++){
                if(all.get(i) instanceof  JSONArray) {
                    prog = new JSONArray(all.get(i).toString());
                    for(int j = 0; j < prog.length(); j++)
                        Log.d("JSON PROG IN ARRAY", prog.get(j).toString());
                }else
                    Log.d("JSON VAL", all.get(i).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            unixStart = prog.getLong(0);
            unixEnd = prog.getLong(0) + prog.getLong(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

            startTime = new java.util.Date(unixStart*1000);
            endTime = new java.util.Date(unixEnd*1000);

        try {
            Log.d("JSON NAME" , prog.getString(3) + " " + prog.getString(4) + " -- " + startTime.toString() + " bis " +  endTime.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }



        return ProgramList;
    }

}
