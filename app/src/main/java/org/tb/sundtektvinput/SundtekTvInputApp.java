package org.tb.sundtektvinput;

import android.app.Application;
import android.content.Context;

import okhttp3.OkHttpClient;


public class SundtekTvInputApp extends Application {

    private static Context context;
    public static OkHttpClient httpClient;

    public void onCreate(){
        super.onCreate();
        SundtekTvInputApp.context = getApplicationContext();
        httpClient = new OkHttpClient();
    }

    public static Context getContext() {
        return SundtekTvInputApp.context;
    }
}
