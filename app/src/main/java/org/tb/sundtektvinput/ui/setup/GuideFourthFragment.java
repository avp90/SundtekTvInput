package org.tb.sundtektvinput.ui.setup;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedActionsStylist;

import org.tb.sundtektvinput.R;

import java.util.List;

/**
 * Created on 07.03.2017.
 */

public class GuideFourthFragment extends GuideBaseFragment {

    private static final int ACTION_ID_PROCESSING = 1;
    private final Handler mFakeHttpHandler = new Handler();


    String title = "Scanning EPG";
    String description = "This can take some time... please wait";

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(title, description, breadcrumb, null);
        return guidance;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Fake Http call by creating some sort of delay.
        mFakeHttpHandler.postDelayed(fakeHttpRequestRunnable, 4000L);
    }

    @Override
    public GuidedActionsStylist onCreateActionsStylist() {
        GuidedActionsStylist stylist = new GuidedActionsStylist() {
            @Override
            public int onProvideItemLayoutId() {
                return R.layout.setup_epg_layout;
            }

        };
        return stylist;
    }

    @Override
    public int onProvideTheme() {
        return R.style.Theme_SetupWizard_NoSelector;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Make sure to cancel the execution of the Runnable in case the fragment is stopped.
        mFakeHttpHandler.removeCallbacks(fakeHttpRequestRunnable);
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

    private final Runnable fakeHttpRequestRunnable = new Runnable() {
        @Override
        public void run() {
            GuidedStepFragment fragment = new GuideFifthFragment();
            fragment.setArguments(getArguments());
            add(getFragmentManager(), fragment);
        }
    };

}

