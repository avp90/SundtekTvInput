package org.tb.sundtektvinput.ui.setup;

import android.annotation.SuppressLint;
import android.media.tv.TvInputInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import android.widget.Toast;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.R;
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
    private String mGroup;
    private ArrayList<GuidedAction> mChannelActions = new ArrayList<>();

    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = getString(R.string.channels_select_title);
        String description = getString(R.string.channels_select_description);
        //    Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, null);
    }

    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CONTINUE)
                .title(getString(R.string.channels_select_action_fetch_selected))
                .build());
        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CONTINUE)
                .title(getString(R.string.channels_select_action_fetch_all))
                .build());

        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CANCEL)
                .title(getString(R.string.channels_select_action_go_back))
                .build());
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        mGroup = getArguments().getString((getContext().getString(R.string.selected_group)));
        if (mGroup == null || mGroup.isEmpty()) {
            getFragmentManager().popBackStack();
            Toast.makeText(getActivity(), R.string.no_group_selected, Toast.LENGTH_LONG).show();
        } else {
            mAllChannels = getChannels(mGroup);
            mSelectedChannels = new SettingsHelper(getActivity()).loadSelectedChannels(mGroup);
            mChannelActions.clear();

            long id;
            for (Channel channel : mAllChannels.values()) {
                id = (long)channel.getOriginalNetworkId();
                GuidedAction action = new GuidedAction.Builder(getActivity())
                        .checkSetId(CHECKBOX_CHECK_SET_ID)
                        .description(channel.getDisplayNumber())
                        .id(id)
                        .title(channel.getDisplayName())
                        .checked(mSelectedChannels.contains(id))
                        .build();
                actions.add(action);
                mChannelActions.add(action);
            }
        }
    }

    //TODO: do it async
    private HashMap<Long, Channel> getChannels(String selectedList) {
        ArrayList<Channel> resp = new ArrayList<>();
        try {
            resp = new FetchChannelsAsync().execute(selectedList).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        mAllChannels = new HashMap<>();
        if (resp != null)
            for (Channel channel : resp)
                mAllChannels.put((long) channel.getOriginalNetworkId(), channel);

        return mAllChannels;
    }

    private class FetchChannelsAsync extends AsyncTask<String, Void, ArrayList<Channel>> {
        @Override
        protected ArrayList<Channel> doInBackground(String... params) {
            String groups = params[0];
            return getApp().getSundtekJsonParser().getChannels(groups);
        }

        @Override
        protected void onPostExecute(ArrayList<Channel> channels) {
            super.onPostExecute(channels);
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();
        FragmentActivity activity = getActivity();
        long id = action.getId();

        if (action.isChecked()) {
            mSelectedChannels.add(id);
        } else if (mSelectedChannels.contains(id)) {
            mSelectedChannels.remove(id);
        } else if (id == ACTION_ID_CONTINUE) {
            if (action.getTitle() == getString(R.string.channels_select_action_fetch_selected)) {
                new SettingsHelper(activity).saveSelectedChannels(mGroup, mSelectedChannels);
            } else if (action.getTitle() == getString(R.string.channels_select_action_fetch_all)) {
                //select all channels
                mSelectedChannels.clear();
                for (GuidedAction channelAction : mChannelActions) {
                    channelAction.setChecked(true);
                    long channelId = channelAction.getId();
                    mSelectedChannels.add(channelId);
                }
                new SettingsHelper(activity).saveSelectedChannels(mGroup, mSelectedChannels);
            }

            String inputId = activity.getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
            if (inputId == null) {
                Toast.makeText(activity, R.string.settings_saved, Toast.LENGTH_LONG).show();
                activity.finish();
            }

            SetupBaseFragment fragment = new EpgScanFragment();
            GuidedStepSupportFragment.add(fm, fragment);
        } else if (id == ACTION_ID_CANCEL) {
            getFragmentManager().popBackStack();
        }
    }
}
