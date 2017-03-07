package org.tb.sundtektvinput.ui.setup;

import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.util.Log;
import android.widget.Toast;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.support.v17.leanback.widget.GuidedAction.NO_CHECK_SET;

public class GuideThirdFragment extends GuideBaseFragment {


    HashMap<String, Channel> channels;

    @Override
    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Channels Selected";
        String description = "";
        Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }


    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        channels = (HashMap<String, Channel>) getArguments().getSerializable("channels");

        List<GuidedAction> subActions = new ArrayList<>();
        for (Channel channel : channels.values()) {
            Log.d("CHANNELRES", channel.getDisplayName());

            subActions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                    .checkSetId(NO_CHECK_SET)
                    .title(channel.getDisplayName())
                    .description(String.valueOf(channel.getOriginalNetworkId()))
                    .checked(true)
                    .focusable(true)
                    .enabled(true)
                    .infoOnly(true)
                    .build());
        }
        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(12)
                .title("Sync EPG now")
                .build());
        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(777)
                .title("Your Channels")
                .subActions(subActions)
                .build());
        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(13)
                .title("Back")
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        if (action.getId() == 12) {
            Toast.makeText(getActivity().getApplicationContext(), "SYNC EPG NOW", Toast.LENGTH_LONG).show();
            Bundle args = new Bundle();
            args.putSerializable("channels",channels);
            GuideBaseFragment fragment = new GuideFourthFragment();
            fragment.setArguments(args);
            GuidedStepFragment.add(fm, fragment);
        }

        if (action.getId() == 13) {
            Toast.makeText(getActivity().getApplicationContext(), "Next", Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
        }
    }


}
