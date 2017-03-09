package org.tb.sundtektvinput.ui.setup;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.ui.setup.base.SetupBaseFragment;

import java.util.HashMap;
import java.util.List;


import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CANCEL;
import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CONTINUE;
import static android.support.v17.leanback.widget.GuidedAction.NO_CHECK_SET;

public class ChannelNumbersFragment extends SetupBaseFragment {


    HashMap<String, Channel> channels;

    public static final long ACTION_ID_CHANNELS = -33;

    @Override
    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Your Channels";
        String description = "You can now review your selection. This is the last time you can go back!!!";
        //   Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, null);
    }


    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CONTINUE)
                .title("Scan EPG")
                .build());

        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CANCEL)
                .title("Back")
                .build());
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        channels = (HashMap<String, Channel>) getArguments().getSerializable("channels");
        int channelNumber = 1;
        for (Channel channel : channels.values()) {
            actions.add(new GuidedAction.Builder(getActivity())
                    .checkSetId(NO_CHECK_SET)
                    .title(channel.getDisplayName())
                    .description(String.valueOf(channelNumber++))
                    .checked(false)
                    .focusable(true)
                    .enabled(true)
                    .descriptionEditable(true)
                    .build());
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        if (action.getId() == ACTION_ID_CONTINUE) {
            Bundle args = new Bundle();
            SetupBaseFragment fragment = new EpgScanFragment();
            GuidedStepFragment.add(fm, fragment);
        }
        if (action.getId() == ACTION_ID_CANCEL) {
            getFragmentManager().popBackStack();
        }
    }


}
