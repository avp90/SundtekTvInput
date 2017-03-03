package com.example.android.sampletvinput.Model;

import android.content.Context;
import android.util.Log;

import com.example.android.sampletvinput.JsonParser.Parser;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;

import java.util.Date;
import java.util.List;

/**
 * Created by Tamim Baschour on 27.02.2017.
 */

public class ProgramsDB {

    private static String TAG = ProgramsDB.class.getSimpleName();

    private Context context;

    private static ProgramsDB myProgramsDB;
    private Parser parser;
    private static List<Program> programs;

    private long lastUpdate;
    private static long MAX_AGE = 43200000;

    public static ProgramsDB getInstance(Context context) {
        if (myProgramsDB == null) {
            myProgramsDB = new ProgramsDB(context);
            myProgramsDB.parser = new Parser();
        }
        return myProgramsDB;
    }

    private ProgramsDB(Context context) {
        Log.d(TAG, "new ProgramsDB Instance");
        this.context = context;
    }


    public List<Program> getPrograms(Channel channel, Boolean forceRefresh) {
        if (programs == null) {
            programs = parser.getPrograms(channel, true);
            lastUpdate = new Date().getTime();
            Log.d(TAG, "programs is null... refreshing");
        } else if ((lastUpdate + MAX_AGE) <= (new Date().getTime())) {
            programs = parser.getPrograms(channel, true);
            lastUpdate = new Date().getTime();
            Log.d(TAG, "programs too old... refreshing");
        }else
            programs = parser.getPrograms(channel, forceRefresh);

        Log.d(TAG, "Found " + programs.size() + " Channels");
        return programs;
    }


    public void reset() {
        programs = null;
    }

}
