/*
 * Copyright 2015 The Android Open Source Project
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

package org.tb.sundtektvinput.service;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Point;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.media.tv.companionlibrary.TvPlayer;
import com.google.android.media.tv.companionlibrary.model.Advertisement;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.model.RecordedProgram;
import com.google.android.media.tv.companionlibrary.utils.TvContractUtils;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.player.MediaPlayer;
import org.tb.sundtektvinput.player.RendererBuilderFactory;
import org.tb.sundtektvinput.service.base.BaseTvInputService;
import org.tb.sundtektvinput.service.base.EpgSyncJobService;

import java.util.ArrayList;
import java.util.List;

import static org.tb.sundtektvinput.parser.SundtekJsonParser.CHANNEL_IPD_MEDIA_TYPE;
import static org.tb.sundtektvinput.parser.SundtekJsonParser.CHANNEL_IPD_MEDIA_URL;

// import com.google.android.media.tv.companionlibrary.EpgSyncJobService;
// import org.tb.sundtektvinput.service.base.EpgSyncJobService;

/**
 * TvInputService which provides a full implementation of EPG, subtitles, multi-audio, parental
 * controls, and overlay view.
 */
public class TvInputService extends BaseTvInputService {
    private static final String TAG = TvInputService.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final long EPG_SYNC_DELAYED_PERIOD_MS = 1000 * 2; // 2 Seconds

    private CaptioningManager mCaptioningManager;

    /**
     * Gets the track id of the track type and track index.
     *
     * @param trackType  the type of the track e.g. TvTrackInfo.TYPE_AUDIO
     * @param trackIndex the index of that track within the media. e.g. 0, 1, 2...
     * @return the track id for the type & index combination.
     */
    private static String getTrackId(int trackType, int trackIndex) {
        return trackType + "-" + trackIndex;
    }

    /**
     * Gets the index of the track for a given track id.
     *
     * @param trackId the track id.
     * @return the track index for the given id, as an integer.
     */
    private static int getIndexFromTrackId(String trackId) {
        return Integer.parseInt(trackId.split("-")[1]);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCaptioningManager = (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
    }

    @Override
    public final Session onCreateSession(String inputId) {
        RichTvInputSessionImpl session = new RichTvInputSessionImpl(this, inputId);
        session.setOverlayViewEnabled(true);
        return super.sessionCreated(session);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public android.media.tv.TvInputService.RecordingSession onCreateRecordingSession(String inputId) {
        return new TvInputServiceRecordingSession(this, inputId);
    }

    class RichTvInputSessionImpl extends BaseTvInputService.Session
            implements MediaPlayer.Listener, MediaPlayer.CaptionListener {
        private static final float CAPTION_LINE_HEIGHT_RATIO = 0.0533f;
        private static final int TEXT_UNIT_PIXELS = 0;
        private static final String UNKNOWN_LANGUAGE = "und";

        private int mSelectedSubtitleTrackIndex;
        private SubtitleLayout mSubtitleView;
        private MediaPlayer mPlayer;
        private boolean mCaptionEnabled;
        private String mInputId;
        private Context mContext;

        RichTvInputSessionImpl(Context context, String inputId) {
            super(context, inputId);
            mCaptionEnabled = mCaptioningManager.isEnabled();
            mContext = context;
            mInputId = inputId;
        }

        @Override
        public View onCreateOverlayView() {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            mSubtitleView = (SubtitleLayout) inflater.inflate(R.layout.subtitleview, null);

            // Configure the subtitle view.
            CaptionStyleCompat captionStyle;
            float captionTextSize = getCaptionFontSize();
            captionStyle = CaptionStyleCompat.createFromCaptionStyle(mCaptioningManager.getUserStyle());
            captionTextSize *= mCaptioningManager.getFontScale();
            mSubtitleView.setStyle(captionStyle);
            mSubtitleView.setFixedTextSize(TEXT_UNIT_PIXELS, captionTextSize);
            mSubtitleView.setVisibility(View.VISIBLE);

            return mSubtitleView;
        }

        private List<TvTrackInfo> getAllTracks() {
            String trackId;
            List<TvTrackInfo> tracks = new ArrayList<>();

            int[] trackTypes = {MediaPlayer.TYPE_AUDIO, MediaPlayer.TYPE_VIDEO, MediaPlayer.TYPE_TEXT};

            for (int trackType : trackTypes) {
                int count = mPlayer.getTrackCount(trackType);
                for (int i = 0; i < count; i++) {
                    MediaFormat format = mPlayer.getTrackFormat(trackType, i);
                    trackId = getTrackId(trackType, i);
                    TvTrackInfo.Builder builder = new TvTrackInfo.Builder(trackType, trackId);

                    if (trackType == MediaPlayer.TYPE_VIDEO) {
                        if (format.maxWidth != MediaFormat.NO_VALUE) {
                            builder.setVideoWidth(format.maxWidth);
                        } else if (format.width != MediaFormat.NO_VALUE) {
                            builder.setVideoWidth(format.width);
                        }

                        if (format.maxHeight != MediaFormat.NO_VALUE) {
                            builder.setVideoHeight(format.maxHeight);
                        } else if (format.height != MediaFormat.NO_VALUE) {
                            builder.setVideoHeight(format.height);
                        }
                    } else if (trackType == MediaPlayer.TYPE_AUDIO) {
                        builder.setAudioChannelCount(format.channelCount);
                        builder.setAudioSampleRate(format.sampleRate);
                        if (format.language != null && !UNKNOWN_LANGUAGE.equals(format.language)) {
                            // TvInputInfo expects {@code null} for unknown language.
                            builder.setLanguage(format.language);
                        }
                    } else if (trackType == MediaPlayer.TYPE_TEXT) {
                        if (format.language != null && !UNKNOWN_LANGUAGE.equals(format.language)) {
                            // TvInputInfo expects {@code null} for unknown language.
                            builder.setLanguage(format.language);
                        }
                    }

                    tracks.add(builder.build());
                }
            }
            return tracks;
        }

        @Override
        public void onPlayChannel(Channel channel) {
            try {
                Log.d(TAG, channel.getInternalProviderData().get(CHANNEL_IPD_MEDIA_TYPE).toString());
                Log.d(TAG, channel.getInternalProviderData().get(CHANNEL_IPD_MEDIA_URL).toString());

                createPlayer(
                        Integer.valueOf(
                                channel.getInternalProviderData().get(CHANNEL_IPD_MEDIA_TYPE).toString()),
                        Uri.parse(
                                String.valueOf(channel.getInternalProviderData().get(CHANNEL_IPD_MEDIA_URL))));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
                }
                mPlayer.setPlayWhenReady(true);

            } catch (InternalProviderData.ParseException e) {
                Log.d(TAG, "onPlayChannel: Could not get media for channel " + channel.getDisplayName());
                e.printStackTrace();
            }
        }

        @Override
        public boolean onPlayProgram(Channel channel, Program program, long startPosMs) {
            if (program == null) {
                requestEpgSync(getCurrentChannelUri());
                return false;
            }

            //if (startPosMs > 0) {
            //    mPlayer.seekTo(startPosMs);
            //}

            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public boolean onPlayRecordedProgram(RecordedProgram recordedProgram) {
            createPlayer(
                    recordedProgram.getInternalProviderData().getVideoType(),
                    Uri.parse(recordedProgram.getInternalProviderData().getVideoUrl()));

            long recordingStartTime =
                    recordedProgram.getInternalProviderData().getRecordedProgramStartTime();
            mPlayer.seekTo(recordingStartTime - recordedProgram.getStartTimeUtcMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            }
            mPlayer.setPlayWhenReady(true);
            return true;
        }

        public TvPlayer getTvPlayer() {
            return mPlayer;
        }

        @Override
        public boolean onTune(Uri channelUri) {
            if (DEBUG) {
                Log.d(TAG, "Tune to " + channelUri.toString());
            }
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
            releasePlayer();
            return super.onTune(channelUri);
        }

        @Override
        public void onPlayAdvertisement(Advertisement advertisement) {
            createPlayer(
                    TvContractUtils.SOURCE_TYPE_HTTP_PROGRESSIVE, Uri.parse(advertisement.getRequestUrl()));
        }

        private void createPlayer(int videoType, Uri videoUrl) {
            releasePlayer();
            mPlayer =
                    new MediaPlayer(
                            RendererBuilderFactory.createRendererBuilder(mContext, videoType, videoUrl));
            mPlayer.addListener(this);
            mPlayer.setCaptionListener(this);
            mPlayer.prepare();
        }

        @Override
        public void onSetCaptionEnabled(boolean enabled) {
            mCaptionEnabled = enabled;
            if (mPlayer != null) {
                if (mCaptionEnabled) {
                    mPlayer.setSelectedTrack(TvTrackInfo.TYPE_SUBTITLE, mSelectedSubtitleTrackIndex);
                } else {
                    mPlayer.setSelectedTrack(TvTrackInfo.TYPE_SUBTITLE, MediaPlayer.TRACK_DISABLED);
                }
            }
        }

        @Override
        public boolean onSelectTrack(int type, String trackId) {
            if (trackId == null) {
                return true;
            }

            int trackIndex = getIndexFromTrackId(trackId);
            if (mPlayer != null) {
                if (type == TvTrackInfo.TYPE_SUBTITLE) {
                    if (!mCaptionEnabled) {
                        return false;
                    }
                    mSelectedSubtitleTrackIndex = trackIndex;
                }

                mPlayer.setSelectedTrack(type, trackIndex);
                notifyTrackSelected(type, trackId);
                return true;
            }
            return false;
        }

        private void releasePlayer() {
            if (mPlayer != null) {
                mPlayer.removeListener(this);
                mPlayer.setSurface(null);
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        }

        @Override
        public void onRelease() {
            super.onRelease();
            releasePlayer();
        }

        @Override
        public void onBlockContent(TvContentRating rating) {
            super.onBlockContent(rating);
            releasePlayer();
        }

        private float getCaptionFontSize() {
            Display display =
                    ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point displaySize = new Point();
            display.getSize(displaySize);
            return Math.max(
                    getResources().getDimension(R.dimen.subtitle_minimum_font_size),
                    CAPTION_LINE_HEIGHT_RATIO * Math.min(displaySize.x, displaySize.y));
        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            if (mPlayer == null) {
                return;
            }

            if (playWhenReady && playbackState == ExoPlayer.STATE_READY) {
                notifyTracksChanged(getAllTracks());
                String audioId =
                        getTrackId(TvTrackInfo.TYPE_AUDIO, mPlayer.getSelectedTrack(TvTrackInfo.TYPE_AUDIO));
                String videoId =
                        getTrackId(TvTrackInfo.TYPE_VIDEO, mPlayer.getSelectedTrack(TvTrackInfo.TYPE_VIDEO));
                String textId =
                        getTrackId(
                                TvTrackInfo.TYPE_SUBTITLE, mPlayer.getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE));

                notifyTrackSelected(TvTrackInfo.TYPE_AUDIO, audioId);
                notifyTrackSelected(TvTrackInfo.TYPE_VIDEO, videoId);
                notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE, textId);
                notifyVideoAvailable();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && Math.abs(mPlayer.getPlaybackSpeed() - 1) < 0.1
                    && playWhenReady
                    && playbackState == ExoPlayer.STATE_BUFFERING) {
                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING);
            }
        }

        @Override
        public void onVideoSizeChanged(
                int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            // Do nothing.
        }

        @Override
        public void onError(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        @Override
        public void onCues(List<Cue> cues) {
            mSubtitleView.setCues(cues);
        }

        public void requestEpgSync(final Uri channelUri) {
            EpgSyncJobService.requestImmediateSync(
                    TvInputService.this, mInputId, new ComponentName(TvInputService.this, EpgJobService.class));
            new Handler(Looper.getMainLooper())
                    .postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    onTune(channelUri);
                                }
                            },
                            EPG_SYNC_DELAYED_PERIOD_MS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private class TvInputServiceRecordingSession extends BaseTvInputService.RecordingSession {
        private static final String TAG = "RecordingSession";
        private String mInputId;
        private long mStartTimeMs;

        public TvInputServiceRecordingSession(Context context, String inputId) {
            super(context, inputId);
            mInputId = inputId;
        }

        @Override
        public void onTune(Uri uri) {
            super.onTune(uri);
            if (DEBUG) {
                Log.d(TAG, "Tune recording session to " + uri);
            }
            // By default, the number of tuners for this service is one. When a channel is being
            // recorded, no other channel from this TvInputService will be accessible. Developers
            // should call notifyError(TvInputManager.RECORDING_ERROR_RESOURCE_BUSY) to alert
            // the framework that this recording cannot be completed.
            // Developers can update the tuner count in xml/tvinputservice or programmatically
            // by adding it to TvInputInfo.updateTvInputInfo.
            notifyTuned(uri);
        }

        @Override
        public void onStartRecording(final Uri uri) {
            super.onStartRecording(uri);
            if (DEBUG) {
                Log.d(TAG, "onStartRecording");
            }
            mStartTimeMs = System.currentTimeMillis();
        }

        @Override
        public void onStopRecording(Program programToRecord) {
            if (DEBUG) {
                Log.d(TAG, "onStopRecording");
            }
            // In this sample app, since all of the content is VOD, the video URL is stored.
            // If the video was live, the start and stop times should be noted using
            // RecordedProgram.Builder.setStartTimeUtcMillis and .setEndTimeUtcMillis.
            // The recordingstart time will be saved in the InternalProviderData.
            // Additionally, the stream should be recorded and saved as
            // a new file.
            long currentTime = System.currentTimeMillis();
            InternalProviderData internalProviderData = programToRecord.getInternalProviderData();
            internalProviderData.setRecordingStartTime(mStartTimeMs);
            RecordedProgram recordedProgram =
                    new RecordedProgram.Builder(programToRecord)
                            .setInputId(mInputId)
                            .setRecordingDataUri(programToRecord.getInternalProviderData().getVideoUrl())
                            .setRecordingDurationMillis(currentTime - mStartTimeMs)
                            .setInternalProviderData(internalProviderData)
                            .build();
            notifyRecordingStopped(recordedProgram);
        }

        @Override
        public void onStopRecordingChannel(Channel channelToRecord) {
            if (DEBUG) {
                Log.d(TAG, "onStopRecording");
            }
            // Program sources in this sample always include program info, so execution here
            // indicates an error.
            notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
        }

        @Override
        public void onRelease() {
            if (DEBUG) {
                Log.d(TAG, "onRelease");
            }
        }
    }
}
