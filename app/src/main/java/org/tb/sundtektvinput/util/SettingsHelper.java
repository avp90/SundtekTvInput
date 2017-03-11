package org.tb.sundtektvinput.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tb.sundtektvinput.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;


/**
 * Created on 06.03.2017.
 */

public class SettingsHelper {

    private static final String CONFIG_FILE = "channelConfig";

    private final Context context;

    public SettingsHelper(Context context) {
        this.context = context;
    }

    public void saveChannelConfig(TreeMap<String, Long> input) {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        if (pSharedPref != null) {
            JSONObject jsonObject = new JSONObject(input);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove("channelConfig").commit();
            editor.putString("channelConfig", jsonString);
            editor.commit();
        }
    }

    public TreeMap<String, Long> loadChannelConfig() {
        TreeMap<String, Long> outputMap = new TreeMap<>();
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        try {
            if (pSharedPref != null) {
                String jsonString = pSharedPref.getString("channelConfig", (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    long value = jsonObject.getLong(key);
                    outputMap.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputMap;
    }


    public void saveChannelIds(ArrayList<String> input) {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        if (pSharedPref != null) {
            JSONArray jsonArray = new JSONArray(input);
            String jsonString = jsonArray.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove("selectedChannels").commit();
            editor.putString("selectedChannels", jsonString);
            editor.commit();
        }
    }

    public ArrayList<String> loadSelectedChannelsMap() {
        ArrayList<String> outputArraylist = new ArrayList<>();
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        try {
            if (pSharedPref != null) {
                String jsonString = pSharedPref.getString("selectedChannels", new JSONArray().toString());
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++)
                    outputArraylist.add(jsonArray.getString(i));
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return outputArraylist;
    }

    public void saveIp(String ip) {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pSharedPref.edit();
        editor.remove(context.getString(R.string.ipsave)).commit();
        editor.putString(context.getString(R.string.ipsave), String.valueOf(ip)).commit();
        editor.commit();
    }

    public String loadIp() {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        return pSharedPref.getString(context.getString(R.string.ipsave), "");
    }

}
