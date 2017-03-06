package org.tb.sundtektvinput.ui;

import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.util.Log;
import android.widget.Toast;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.parser.SundtekJsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.support.v17.leanback.widget.GuidedAction.CHECKBOX_CHECK_SET_ID;

public class GuideSecondFragment extends GuidedStepFragment {

    HashMap<String, Channel> selectedChannels = new HashMap<>();
    HashMap<String, Channel> allChannels;

    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Select Channels";
        String breadcrumb = "Sundtek Tv Input Setup";
        String description = "";
        Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(0)
                .title("Start")
                .description("Channels will be pulled from server")
                .build());

        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(1)
                .title("Back")
                .build());
    }


    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        allChannels = getChannels();

        for (Channel channel : allChannels.values()) {
            actions.add(
                    new GuidedAction.Builder(getActivity().getApplicationContext())
                            .checkSetId(CHECKBOX_CHECK_SET_ID)
                            .description(String.valueOf(channel.getOriginalNetworkId()))
                            .title(channel.getDisplayName())
                            .build());
        }
    }


    HashMap<String, Channel> getChannels() {
        ArrayList<Channel> resp = new ArrayList<>();
        try {
            resp = new getChannelsAsync().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        allChannels = new HashMap<>();
        for (Channel channel : resp)
            allChannels.put(String.valueOf(channel.getOriginalNetworkId()), channel);

        return allChannels;
    }

    private class getChannelsAsync extends AsyncTask<Void, Void, ArrayList<Channel>> {

        @Override
        protected ArrayList<Channel> doInBackground(Void... arg0) {
            return new SundtekJsonParser().getChannels();
        }

        @Override
        protected void onPostExecute(ArrayList<Channel> channels) {
            super.onPostExecute(channels);
        }
    }


    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        if (action.isChecked()) {
            selectedChannels.put(action.getDescription().toString(), allChannels.get(action.getDescription()));
            Log.d("ACTIONID", action.getTitle() + " in list " + selectedChannels.containsKey(action.getDescription()));
        } else {
            if (selectedChannels.containsKey(action.getDescription())) {
                selectedChannels.remove(action.getDescription());
                Log.d("ACTIONID", action.getTitle() + " in list " + selectedChannels.containsKey(action.getDescription()));
            }
        }

        if (action.getId() == 0) {
            Toast.makeText(getActivity().getApplicationContext(), "Next", Toast.LENGTH_LONG).show();
        }
        if (action.getId() == 1) {
            Toast.makeText(getActivity().getApplicationContext(), "BACK", Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
        }
    }

}
