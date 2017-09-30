package org.tb.sundtektvinput.ui.setup;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.media.tv.TvInputInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.widget.Toast;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.SundtekTvInputApp;
import org.tb.sundtektvinput.parser.SundtekJsonParser;
import org.tb.sundtektvinput.ui.setup.base.SetupBaseFragment;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;


import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CANCEL;
import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CONTINUE;
import static android.support.v17.leanback.widget.GuidedAction.CHECKBOX_CHECK_SET_ID;

public class ChannelSelectFragment extends SetupBaseFragment {

    ArrayList<Long> selectedChannels = new ArrayList<>();
    HashMap<Long, Channel> allChannels;
    HashMap<Long, Channel> selectedChannelMap;
    String selectedList;


    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Here we go";
        String description = "Please select the channels you want to import";
        //    Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, null);
    }


    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CONTINUE)
                .title("Fetch EPG")
                .build());

        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CANCEL)
                .title("Back")
                .build());
    }


    @SuppressLint("UseSparseArrays")
    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        selectedList = getArguments().getString((getContext().getString(R.string.selected_list)));
        if (selectedList.isEmpty()) {
            getFragmentManager().popBackStack();
            Toast.makeText(getActivity(), "No list selected", Toast.LENGTH_LONG).show();
        } else {


            allChannels = getChannels(selectedList);
            selectedChannels = new SettingsHelper(getActivity()).loadSelectedChannels(selectedList);
            selectedChannelMap = new HashMap<>();

            long id;
            for (Channel channel : allChannels.values()) {
                id = (long) channel.getOriginalNetworkId();

                if (selectedChannels.contains(id)) {
                    selectedChannelMap.put(id, channel);
                }
                actions.add(
                        new GuidedAction.Builder(getActivity())
                                .checkSetId(CHECKBOX_CHECK_SET_ID)
                                .description(channel.getDisplayNumber())
                                .id(id)
                                .title(channel.getDisplayName())
                                .checked(selectedChannels.contains(id))
                                .build());
            }
        }
    }


    HashMap<Long, Channel> getChannels(String selectedList) {
        ArrayList<Channel> resp = new ArrayList<>();
        try {
            resp = new getChannelsAsync().execute(selectedList).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        allChannels = new HashMap<>();
        if (resp != null)
            for (Channel channel : resp)
                allChannels.put((long) channel.getOriginalNetworkId(), channel);

        return allChannels;
    }

    private static class getChannelsAsync extends AsyncTask<String, Void, ArrayList<Channel>> {

        @Override
        protected ArrayList<Channel> doInBackground(String... params) {
            String selectedList = params[0];
            return new SundtekJsonParser(SundtekTvInputApp.getContext()).getChannels(selectedList);
        }

        @Override
        protected void onPostExecute(ArrayList<Channel> channels) {
            super.onPostExecute(channels);
        }
    }


    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        long id = action.getId();
        if (action.isChecked()) {
            selectedChannels.add(id);
            selectedChannelMap.put(id, allChannels.get(id));
        } else {
            if (selectedChannels.contains(id)) {
                selectedChannels.remove(id);
                selectedChannelMap.remove(id);
            }
        }

        if (action.getId() == ACTION_ID_CONTINUE) {
            new SettingsHelper(getActivity())
                    .saveSelectedChannels(selectedList, selectedChannels);

            String inputId = getActivity().getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);

            if (inputId == null) {
                Toast.makeText(getActivity(), "Settings Saved", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }

            SetupBaseFragment fragment = new EpgScanFragment();
            GuidedStepFragment.add(fm, fragment);


        }
        if (action.getId() == ACTION_ID_CANCEL) {
            getFragmentManager().popBackStack();
        }
    }
}
