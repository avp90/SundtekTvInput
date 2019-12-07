package org.tb.sundtektvinput.ui.setup;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import android.widget.Toast;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.ui.setup.base.SetupBaseFragment;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static androidx.leanback.widget.GuidedAction.ACTION_ID_CANCEL;
import static androidx.leanback.widget.GuidedAction.ACTION_ID_CONTINUE;
import static androidx.leanback.widget.GuidedAction.ACTION_ID_CURRENT;
import static androidx.leanback.widget.GuidedAction.DEFAULT_CHECK_SET_ID;

public class ChannelGroupSelectFragment extends SetupBaseFragment {
    String selectedGroup;
    ArrayList<String> allGroups;

    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Select a Group";
        String description = "Please select the Channel Group you want to import.";
        //    Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, null);
    }

    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CONTINUE)
                .title("Continue")
                .build());

        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CANCEL)
                .title("Back")
                .build());
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        allGroups = getAvailableGroups();
        selectedGroup = new SettingsHelper(getActivity()).loadSelectedGroup();

        if (allGroups.isEmpty()) {
            getFragmentManager().popBackStack();
            Toast.makeText(getActivity(), "Could not find channel list at at given ip", Toast.LENGTH_LONG).show();
        }

        for (String list : allGroups) {
            actions.add(
                    new GuidedAction.Builder(getActivity())
                            .checkSetId(DEFAULT_CHECK_SET_ID)
                            .id(list.hashCode())
                            .checked(selectedGroup.equals(list))
                            .title(list)
                            .build());
        }
    }


    //TODO: do it async
    ArrayList<String> getAvailableGroups() {
        try {
            allGroups = new FetchGroupsAsync().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return allGroups;
    }

    private class FetchGroupsAsync extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... arg0) {
            return allGroups = getApp().getSundtekJsonParser().getListsAvailable();
        }

        @Override
        protected void onPostExecute(ArrayList<String> lists) {
            super.onPostExecute(allGroups);
        }
    }


    @Override
    public long onGuidedActionEditedAndProceed(GuidedAction action) {
        return ACTION_ID_CURRENT;
    }


    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        if (allGroups.contains(action.getTitle())) {
            selectedGroup = action.getTitle().toString();
        }

        if (action.getId() == ACTION_ID_CONTINUE) {

            if (!selectedGroup.isEmpty()) {
                new SettingsHelper(getActivity())
                        .saveSelectedGroup(selectedGroup);
                Bundle args = new Bundle();
                args.putString(getContext().getString(R.string.selected_group), selectedGroup);

                SetupBaseFragment fragment = new ChannelSelectFragment();
                fragment.setArguments(args);
                GuidedStepSupportFragment.add(fm, fragment);
            } else {
                Toast.makeText(getActivity(), "You need to select a list"
                        , Toast.LENGTH_LONG).show();
            }

        }

        if (action.getId() == ACTION_ID_CANCEL) {
            getFragmentManager().popBackStack();
        }


    }
}
