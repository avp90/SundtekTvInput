package org.tb.sundtektvinput.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.tb.sundtektvinput.R;

import java.util.ArrayList;


/**
 * Created on 06.03.2017.
 */

public class SettingsHelper {

    private final Context context;

    public SettingsHelper(Context context) {
        this.context = context;
    }

    public void saveChannelIds(ArrayList<String> input, String fileName) {
        SharedPreferences pSharedPref = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if (pSharedPref != null) {
            JSONArray jsonArray = new JSONArray(input);
            String jsonString = jsonArray.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove("selectedChannels").commit();
            editor.putString("selectedChannels", jsonString);
            editor.commit();
        }
    }

    public ArrayList<String> loadSelectedChannelsMap(String fileName) {
        ArrayList<String> outputArraylist = new ArrayList<>();
        SharedPreferences pSharedPref = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        try {
            if (pSharedPref != null) {
                String jsonString = pSharedPref.getString("selectedChannels", new JSONArray().toString());
                JSONArray jsonArray = new JSONArray(jsonString);
                for(int i = 0 ; i < jsonArray.length(); i++)
                    outputArraylist.add(jsonArray.getString(i));
                }
            } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return outputArraylist;
    }

    public void saveIp(String ip){
        SharedPreferences pSharedPref = context.getSharedPreferences(context.getString(R.string.ipsave), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pSharedPref.edit();
        editor.remove(context.getString(R.string.ipsave)).commit();
        editor.putString(context.getString(R.string.ipsave), String.valueOf(ip)).commit();
        editor.commit();
    }

    public String loadIp(){
        SharedPreferences pSharedPref = context.getSharedPreferences(context.getString(R.string.ipsave), Context.MODE_PRIVATE);
        return pSharedPref.getString(context.getString(R.string.ipsave), "");
    }

}
