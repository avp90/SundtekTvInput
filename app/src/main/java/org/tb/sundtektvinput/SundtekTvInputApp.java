package org.tb.sundtektvinput;

import android.app.Application;
import android.content.Context;

import org.tb.sundtektvinput.model.ChannelsDB;
import org.tb.sundtektvinput.model.ProgramsDB;
import org.tb.sundtektvinput.parser.SundtekJsonParser;

import okhttp3.OkHttpClient;

public class SundtekTvInputApp extends Application {
    private OkHttpClient mHttpClient;
    private ProgramsDB mProgramsDB;
    private ChannelsDB mChannelsDB;
    private SundtekJsonParser mSundtekJsonParser;

    public void onCreate() {
        super.onCreate();
    }


    public ChannelsDB getChannelsDB() {
        if (mChannelsDB == null) {
            mChannelsDB = new ChannelsDB(this);
        }
        return mChannelsDB;
    }

    public SundtekJsonParser getSundtekJsonParser() {
        if (mSundtekJsonParser == null) {
            mSundtekJsonParser = new SundtekJsonParser(this);
        }
        return mSundtekJsonParser;
    }

    public OkHttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new OkHttpClient();
        }
        return mHttpClient;
    }

    public ProgramsDB getProgramsDB() {
        if (mProgramsDB == null) {
            mProgramsDB = new ProgramsDB(this);
        }
        return mProgramsDB;
    }
}
