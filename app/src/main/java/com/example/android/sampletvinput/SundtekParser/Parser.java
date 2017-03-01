package com.example.android.sampletvinput.SundtekParser;

import android.content.Context;
import android.os.AsyncTask;
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

    Context context;
    public Parser(Context context){
        this.context = context;

    }


    public Object test(){
        GsonBuilder builder = new GsonBuilder();
        Object o = builder.create().fromJson(CHANNELS, Object.class);
        Log.d("GSON", o.toString());

        return o;
    }

    public List<Channel> getChannels()  {
        List<Channel> channelList = new ArrayList<>();
        InternalProviderData internalProviderData = new InternalProviderData();
        Log.d("TESTEST" , "internalProviderData");
        JSONArray jArray;
        try {
            Log.d("TESTEST" , "try");

   //         jArray = new JSONArray(new JsonTask().execute("http://192.168.3.1:22000/channels.json").get());
            jArray = new JSONArray(getJson("http://192.168.3.1:22000/channels.json"));
            Log.d("TESTEST" , "JsonTask");

            for (int i = 0; i < 2; i++) {
                Log.d("TESTEST" , "for");

                //     channelList.add(new SundtekChannel(jArray.getJSONArray(i)));
                channelList.add(
                        new Channel.Builder()
                                .setDisplayName(jArray.getJSONArray(i).get(0).toString())
                                .setDisplayNumber(Integer.toString(i))
                                .setChannelLogo(jArray.getJSONArray(i).get(2).toString())
                                .setOriginalNetworkId(101)
                                .setInternalProviderData(internalProviderData)
                                .setServiceId(Integer.parseInt(jArray.getJSONArray(i).get(4).toString().substring(0 ,jArray.getJSONArray(i).get(4).toString().indexOf("_"))))
                                .setPackageName(context.getPackageName())
                                .build());
                Log.d("TESTEST" , " channelList.add");

//                for ( int j = 0; j < jArray.getJSONArray(i).length(); j++){
//                    Log.d("JSON" ,  jArray.getJSONArray(i).get(j).toString());
//                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (Channel channel : channelList) {
            Log.d("CHANNEL", channel.toString());
        }

        return channelList;
    }

    private String getJson(String surl) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

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
        private class JsonTask extends AsyncTask<String, String, String> {


        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

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
