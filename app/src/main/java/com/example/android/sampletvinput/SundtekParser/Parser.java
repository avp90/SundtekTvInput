package com.example.android.sampletvinput.SundtekParser;

import android.util.Log;

import com.example.android.sampletvinput.SundtekParser.Model.SundtekChannel;
import com.example.android.sampletvinput.SundtekParser.Model.SundtekProgram;

import org.json.JSONArray;
import org.json.JSONException;

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

    public List<SundtekChannel> getChannels() {
        List<SundtekChannel> channelList = new ArrayList<>();
        try {
            JSONArray jArray = new JSONArray(CHANNELS);

            for (int i = 0; i < jArray.length(); i++) {
                channelList.add(new SundtekChannel(jArray.getJSONArray(i)));
                Log.d("JSON CHANNELS", channelList.get(i).getChannelName());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return channelList;
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
