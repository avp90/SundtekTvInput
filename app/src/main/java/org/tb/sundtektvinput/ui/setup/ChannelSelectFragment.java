package org.tb.sundtektvinput.ui.setup;

import android.annotation.SuppressLint;
import android.media.tv.TvInputInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

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


import static androidx.leanback.widget.GuidedAction.ACTION_ID_CANCEL;
import static androidx.leanback.widget.GuidedAction.ACTION_ID_CONTINUE;
import static androidx.leanback.widget.GuidedAction.CHECKBOX_CHECK_SET_ID;

public class ChannelSelectFragment extends SetupBaseFragment {
    private ArrayList<Long> mSelectedChannels = new ArrayList<>();
    private HashMap<Long, Channel> mAllChannels;
    private String mSelectedList;

    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Here we go";
        String description = "Please select the channels you want to import.";
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
        mSelectedList = getArguments().getString((getContext().getString(R.string.active_list)));
        if (mSelectedList.isEmpty()) {
            getFragmentManager().popBackStack();
            Toast.makeText(getActivity(), "No list selected", Toast.LENGTH_LONG).show();
        } else {
            mAllChannels = getChannels(mSelectedList);
            mSelectedChannels = new SettingsHelper(getActivity()).loadSelectedChannels(mSelectedList);

            long id;
            for (Channel channel : mAllChannels.values()) {
                id = (long) channel.getOriginalNetworkId();

                actions.add(
                        new GuidedAction.Builder(getActivity())
                                .checkSetId(CHECKBOX_CHECK_SET_ID)
                                .description(channel.getDisplayNumber())
                                .id(id)
                                .title(channel.getDisplayName())
                                .checked(mSelectedChannels.contains(id))
                                .build());
            }
        }
    }

    private HashMap<Long, Channel> getChannels(String selectedList) {
        ArrayList<Channel> resp = new ArrayList<>();
        try {
            resp = new getChannelsAsync().execute(selectedList).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        mAllChannels = new HashMap<>();
        if (resp != null)
            for (Channel channel : resp)
                mAllChannels.put((long) channel.getOriginalNetworkId(), channel);

        return mAllChannels;
    }

    private class getChannelsAsync extends AsyncTask<String, Void, ArrayList<Channel>> {
        @Override
        protected ArrayList<Channel> doInBackground(String... params) {
            String selectedList = params[0];
            return getApp().getSundtekJsonParser().getChannels(selectedList);
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
            mSelectedChannels.add(id);
        } else {
            if (mSelectedChannels.contains(id)) {
                mSelectedChannels.remove(id);
            }
        }

        if (action.getId() == ACTION_ID_CONTINUE) {
            new SettingsHelper(getActivity())
                    .saveSelectedChannels(mSelectedList, mSelectedChannels);

            String inputId = getActivity().getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
            if (inputId == null) {
                Toast.makeText(getActivity(), "Settings Saved", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }

            SetupBaseFragment fragment = new EpgScanFragment();
            GuidedStepSupportFragment.add(fm, fragment);
        }

        if (action.getId() == ACTION_ID_CANCEL) {
            getFragmentManager().popBackStack();
        }
    }
}
