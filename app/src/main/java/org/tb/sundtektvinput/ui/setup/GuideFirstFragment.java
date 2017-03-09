package org.tb.sundtektvinput.ui.setup;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;

import java.util.List;


import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CANCEL;
import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CONTINUE;

public class GuideFirstFragment extends GuideBaseFragment {


    @Override
    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Get all the channels";
        String description = "Press 'Find Channels' to get a list of all available channels from your streamingserver";
        //     Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(ACTION_ID_CONTINUE)
                .title("Find Channels")
                .build());

        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(ACTION_ID_CANCEL)
                .title("Back")
                .build());
    }


    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        if (action.getId() == ACTION_ID_CONTINUE) {
            GuidedStepFragment.add(fm, new GuideSecondFragment());
        }
        if (action.getId() == ACTION_ID_CANCEL) {

            getFragmentManager().popBackStack();
        }
    }


}
