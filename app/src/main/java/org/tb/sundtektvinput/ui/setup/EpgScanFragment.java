package org.tb.sundtektvinput.ui.setup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.tv.TvInputInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedActionsStylist;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.service.MyJobService;
import org.tb.sundtektvinput.service.base.EpgSyncJobService;
import org.tb.sundtektvinput.ui.setup.base.SetupBaseFragment;

import java.util.List;

/**
 * Created on 07.03.2017.
 */

public class EpgScanFragment extends SetupBaseFragment {

    private static final String TAG = EpgScanFragment.class.getSimpleName();

    final static Boolean DEBUG = true;

    private static final int ACTION_ID_PROCESSING = 1;

    String title = "Scanning EPG";
    String description = "This can take some time... please wait";

    public static final long FULL_SYNC_FREQUENCY_MILLIS = 1000 * 60 * 60 * 3;  // 3 hour
    private static final long FULL_SYNC_WINDOW_SEC = 1000 * 60 * 60 * 24 * 14;  // 2 weeks

    private String mInputId = null;
    private boolean mErrorFound;
    private boolean mFinishedScan;


    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(title, description, breadcrumb, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInputId = getActivity().getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mSyncStatusChangedReceiver,
                new IntentFilter(EpgSyncJobService.ACTION_SYNC_STATUS_CHANGED));
        onScanStarted();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public GuidedActionsStylist onCreateActionsStylist() {
        return new GuidedActionsStylist() {
            @Override
            public int onProvideItemLayoutId() {
                return R.layout.setup_epg_layout;
            }
        };
    }


    @Override
    public int onProvideTheme() {
        return R.style.Theme_SetupWizard_NoSelector;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_PROCESSING)
                .title("Scanning")
                .infoOnly(true)
                .build();
        actions.add(action);
    }

    private void updateScanProgress(int channelsScanned, int channelCount, String channelName) {
        if (DEBUG)
            Log.d(TAG, "updateScanProgress: " + channelsScanned + " " + channelCount);

        GuidedAction a = getActions().get(findActionPositionById(ACTION_ID_PROCESSING));
        a.setTitle(channelsScanned + " / " + channelCount + "\t\t" + channelName);
        notifyActionChanged(findActionPositionById(ACTION_ID_PROCESSING));
        onChannelScanCompleted(channelsScanned, channelCount);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mSyncStatusChangedReceiver);
    }

    public void onScanStarted() {
        if (DEBUG)
            Log.d(TAG, "onScanStarted");
        EpgSyncJobService.cancelAllSyncRequests(getActivity());
        EpgSyncJobService.requestImmediateSync(getActivity(), mInputId,
                new ComponentName(getActivity(), MyJobService.class));

        // Set up SharedPreference to share inputId. If there is not periodic sync job after reboot,
        // BootReceiver can use the shared inputId to set up periodic sync job.
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                EpgSyncJobService.PREFERENCE_EPG_SYNC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EpgSyncJobService.BUNDLE_KEY_INPUT_ID, mInputId);
        editor.apply();
    }

    public String getInputId() {
        return mInputId;
    }

    public void onScanFinished() {
        if (DEBUG)
            Log.d(TAG, "onScanFinished");
        if (!mErrorFound) {
            if (DEBUG)
                Log.d(TAG, "onScanFinished: RESULT_OK");
            EpgSyncJobService.cancelAllSyncRequests(getActivity());
            EpgSyncJobService.setUpPeriodicSync(getActivity(), mInputId,
                    new ComponentName(getActivity(), MyJobService.class),
                    FULL_SYNC_FREQUENCY_MILLIS, FULL_SYNC_WINDOW_SEC);
            getActivity().setResult(Activity.RESULT_OK);
        } else {
            if (DEBUG)
                Log.d(TAG, "onScanFinished: RESULT_CANCELED");
            getActivity().setResult(Activity.RESULT_CANCELED);
        }
        GuidedStepFragment fragment = new FinishFragment();
        fragment.setArguments(getArguments());
        add(getFragmentManager(), fragment);
    }


    /**
     * This method will be called when an error occurs in scanning. Developers may want to notify
     * the user that an error has happened or resolve the error.
     *
     * @param reason A constant indicating the type of error that has happened. Possible values are
     *               {@link EpgSyncJobService#ERROR_EPG_SYNC_CANCELED},
     *               {@link EpgSyncJobService#ERROR_INPUT_ID_NULL},
     *               {@link EpgSyncJobService#ERROR_NO_PROGRAMS},
     *               {@link EpgSyncJobService#ERROR_NO_CHANNELS}, or
     *               {@link EpgSyncJobService#ERROR_DATABASE_INSERT},
     */

    public void onScanError(int reason) {
        switch (reason) {
            case EpgSyncJobService.ERROR_EPG_SYNC_CANCELED:
                mErrorFound = true;
                Log.d(TAG, "Scanerror: " + getString(R.string.sync_error_canceled));
                break;
            case EpgSyncJobService.ERROR_NO_PROGRAMS:
                Log.d(TAG, "Scanerror: " + getString(R.string.sync_error_no_programs));
                break;
            case EpgSyncJobService.ERROR_NO_CHANNELS:
                mErrorFound = true;
                Log.d(TAG, "Scanerror: " + getString(R.string.sync_error_no_channels));
                break;
            default:
                mErrorFound = true;
                Log.d(TAG, "Scanerror: " + getString(R.string.sync_error_other, reason));
                break;
        }
    }

    /**
     * Finishes the current scan thread. This fragment will be popped after the scan thread ends.
     *
     * @param scanCompleted a flag which indicates the scan was completed successfully or canceled.
     */
    private void finishScan(boolean scanCompleted) {
        // Hides the cancel button.
        mFinishedScan = true;
        //mCancelButton.setEnabled(false);
        onScanFinished();
    }


    /**
     * This method will be called when a channel has been completely scanned. It can be overriden
     * to display custom information about this channel to the user.
     *
     * @param displayName   {@link Channel#getDisplayName()} for the scanned channel.
     * @param displayNumber {@link Channel#getDisplayNumber()} ()} for the scanned channel.
     */
    public void onScannedChannel(CharSequence displayName, CharSequence displayNumber) {
        if (DEBUG) {
            Log.d(TAG, "onScannedChannel Scanned channel data: " + displayName + ", " + displayNumber);
        }
    }

    /**
     * This method will be called when another channel has been scanned. It can be overriden to
     * display custom information about the current progress of the scan.
     *
     * @param channelsScanned The number of channels that have been scanned so far.
     * @param channelCount    The total number of channels that need to be scanned.
     */
    public void onChannelScanCompleted(int channelsScanned, int channelCount) {
        Log.d(TAG, "onChannelScanCompleted : " + channelsScanned + " " + channelCount);
    }


    private final BroadcastReceiver mSyncStatusChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mFinishedScan) {
                        return;
                    }
                    String syncStatusChangedInputId = intent.getStringExtra(
                            EpgSyncJobService.BUNDLE_KEY_INPUT_ID);
                    if (syncStatusChangedInputId != null
                            && syncStatusChangedInputId.equals(getInputId())) {
                        String syncStatus = intent.getStringExtra(EpgSyncJobService.SYNC_STATUS);
                        if (syncStatus.equals(EpgSyncJobService.SYNC_STARTED)) {
                            if (DEBUG) {
                                Log.d(TAG, "Sync status: Started");
                            }
                        } else if (syncStatus.equals(EpgSyncJobService.SYNC_SCANNED)) {
                            int channelsScanned = intent.
                                    getIntExtra(EpgSyncJobService.BUNDLE_KEY_CHANNELS_SCANNED, 0);
                            int channelCount = intent.
                                    getIntExtra(EpgSyncJobService.BUNDLE_KEY_CHANNEL_COUNT, 0);
                            String channelDisplayName = intent.getStringExtra(
                                    EpgSyncJobService.BUNDLE_KEY_SCANNED_CHANNEL_DISPLAY_NAME);
                            String channelDisplayNumber = intent.getStringExtra(
                                    EpgSyncJobService.BUNDLE_KEY_SCANNED_CHANNEL_DISPLAY_NUMBER);

                            updateScanProgress(++channelsScanned, channelCount, channelDisplayName );

                            if (DEBUG) {
                                Log.d(TAG, "Sync status: Channel Scanned");
                                Log.d(TAG, "Scanned " + channelsScanned + " out of " + channelCount);
                            }
                            onScannedChannel(channelDisplayName, channelDisplayNumber);
                            //   mAdapter.add(new Pair<>(channelDisplayName, channelDisplayNumber));
                        } else if (syncStatus.equals(EpgSyncJobService.SYNC_FINISHED)) {
                            if (DEBUG) {
                                Log.d(TAG, "Sync status: Finished");
                            }
                            finishScan(true);
                        } else if (syncStatus.equals(EpgSyncJobService.SYNC_ERROR)) {
                            int errorCode =
                                    intent.getIntExtra(EpgSyncJobService.BUNDLE_KEY_ERROR_REASON,
                                            0);
                            if (DEBUG) {
                                Log.d(TAG, "Error occurred: " + errorCode);
                            }
                            onScanError(errorCode);
                        }
                    }
                }
            });
        }
    };
}

