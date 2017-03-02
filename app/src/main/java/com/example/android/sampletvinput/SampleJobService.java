/*
 * Copyright 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sampletvinput;

import android.net.Uri;

import com.example.android.sampletvinput.JsonParser.Parser;
import com.google.android.media.tv.companionlibrary.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * EpgSyncJobService that periodically runs to update channels and programs.
 */
public class SampleJobService extends EpgSyncJobService {
    private String MPEG_DASH_CHANNEL_NAME = "MPEG_DASH";
    private String MPEG_DASH_CHANNEL_NUMBER = "3";
    private String MPEG_DASH_CHANNEL_LOGO
            = "https://storage.googleapis.com/android-tv/images/mpeg_dash.png";
    private int MPEG_DASH_ORIGINAL_NETWORK_ID = 101;
    private String TEARS_OF_STEEL_TITLE = "Tears of Steel";
    private String TEARS_OF_STEEL_DESCRIPTION = "Monsters invade a small town in this sci-fi flick";
    private String TEARS_OF_STEEL_ART
            = "https://storage.googleapis.com/gtv-videos-bucket/sample/images/tears.jpg";
    private String TEARS_OF_STEEL_SOURCE
            = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd";
    private static final long TEARS_OF_STEEL_START_TIME_MS = 0;
    private static final long TEARS_OF_STEEL_DURATION_MS = 734 * 1000;
    private static final long TEST_AD_1_START_TIME_MS = 15 * 1000;
    private static final long TEST_AD_2_START_TIME_MS = 40 * 1000;
    private static final long TEST_AD_DURATION_MS = 10 * 1000;
    /**
     * Test <a href="http://www.iab.com/guidelines/digital-video-ad-serving-template-vast-3-0/">
     * VAST</a> URL from <a href="https://www.google.com/dfp">DoubleClick for Publishers (DFP)</a>.
     * More sample VAST tags can be found on
     * <a href="https://developers.google.com/interactive-media-ads/docs/sdks/android/tags">DFP
     * website</a>. You should replace it with the vast tag that you applied from your
     * advertisement provider. To verify whether your video ad response is VAST compliant, try<a
     * href="https://developers.google.com/interactive-media-ads/docs/sdks/android/vastinspector">
     * Google Ads Mobile Video Suite Inspector</a>
     */
    private static String TEST_AD_REQUEST_URL =
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/" +
                    "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast" +
                    "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct" +
                    "%3Dlinear&correlator=";
    private Parser parser;

    @Override
    public List<Channel> getChannels() {
//      Add channels through an XMLTV file
//        List<Channel> channelList;
 //       XmlTvParser.TvListing listings = RichFeedUtil.getRichTvListings(this);
 //       channelList = new ArrayList<>(listings.getChannels());
//        Log.d("CHANNELLIST", channelList.toString());
 //       Log.d("CHANNELLIST", "--------------------------------------------------------------");

        if(parser == null)
            parser = new Parser();
        List<Channel> channelList = null;
        try {
            channelList = new ArrayList<>(parser.getChannels());
        } catch (JSONException e) {
            e.printStackTrace();
        }

//      Log.d("CHANNELLISTXML", channelList.toString());
//      Log.d("CHANNELLISTCODE", channelList2.toString());

        return channelList;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channel, long startMs,
                                               long endMs) {
        List<Program> programsList = null;

//        //Is an XMLTV Channel
//        XmlTvParser.TvListing listings = RichFeedUtil.getRichTvListings(getApplicationContext());
//        return listings.getPrograms(channel);

        if(parser == null)
            parser = new Parser();
        try {
            programsList = new ArrayList<>(parser.getPrograms(channel));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return programsList;

    }
}
