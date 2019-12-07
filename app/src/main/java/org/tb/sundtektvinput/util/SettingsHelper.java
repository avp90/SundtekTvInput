package org.tb.sundtektvinput.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.service.EpgJobService;
import org.tb.sundtektvinput.service.base.EpgSyncJobService;

import java.util.ArrayList;


/**
 * Created on 06.03.2017.
 */

public class SettingsHelper {
    private static final String CONFIG_FILE = "channelConfig";
    private final Context context;

    public SettingsHelper(Context context) {
        this.context = context;
    }

    public void saveSelectedChannels(String list, ArrayList<Long> input) {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        String saveString = context.getString(R.string.save_channels_prefix) + list;

        if (pSharedPref != null) {
            JSONArray jsonArray = new JSONArray(input);
            String jsonString = jsonArray.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove(saveString);
            editor.putString(saveString, jsonString);
            editor.apply();
        }
    }

    public ArrayList<Long> loadSelectedChannels(String list) {
        ArrayList<Long> output = new ArrayList<>();
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        String saveString = context.getString(R.string.save_channels_prefix) + list;
        try {
            if (pSharedPref != null) {
                String jsonString = pSharedPref.getString(saveString, new JSONArray().toString());
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++)
                    output.add(jsonArray.getLong(i));
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return output;
    }

    public void saveSelectedList(String selectedList) {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pSharedPref.edit();
        editor.remove(context.getString(R.string.active_list));
        editor.putString(context.getString(R.string.active_list), selectedList);
        editor.apply();
    }

    public String loadSelectedList() {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        return pSharedPref.getString(context.getString(R.string.active_list), "");
    }

    public void saveLastUpdateTimestamp(long timestamp) {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pSharedPref.edit();
        editor.remove(context.getString(R.string.save_last_update_timestamp));
        editor.putLong(context.getString(R.string.save_last_update_timestamp), timestamp);
        editor.apply();
    }

    public long loadLastUpdateTimestamp() {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        return pSharedPref.getLong(context.getString(R.string.save_last_update_timestamp), 0);
    }


    public void saveIp(String ip) {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pSharedPref.edit();
        editor.remove(context.getString(R.string.save_ip));
        editor.putString(context.getString(R.string.save_ip), ip);
        editor.apply();
    }

    public String loadIp() {
        SharedPreferences pSharedPref = context.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        return pSharedPref.getString(context.getString(R.string.save_ip), context.getString(R.string.default_ip));
    }
}
