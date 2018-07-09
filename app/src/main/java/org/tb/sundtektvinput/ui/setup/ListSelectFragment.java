package org.tb.sundtektvinput.ui.setup;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import android.util.Log;
import android.widget.Toast;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.parser.SundtekJsonParser;
import org.tb.sundtektvinput.ui.setup.base.SetupBaseFragment;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static androidx.leanback.widget.GuidedAction.ACTION_ID_CANCEL;
import static androidx.leanback.widget.GuidedAction.ACTION_ID_CONTINUE;
import static androidx.leanback.widget.GuidedAction.ACTION_ID_CURRENT;
import static androidx.leanback.widget.GuidedAction.DEFAULT_CHECK_SET_ID;

public class ListSelectFragment extends SetupBaseFragment {

    String selectedList;
    ArrayList<String> listsAvailable;

    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Select a list";
        String description = "Please select the channellist you want to import";
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
        listsAvailable = getListsAvailable();
        selectedList = new SettingsHelper(getActivity()).loadSelectedList();

        if (listsAvailable.isEmpty()) {
            getFragmentManager().popBackStack();
            Toast.makeText(getActivity(), "Could not find channel list at at given ip", Toast.LENGTH_LONG).show();
        }

        for (String list : listsAvailable) {
            actions.add(
                    new GuidedAction.Builder(getActivity())
                            .checkSetId(DEFAULT_CHECK_SET_ID)
                            .id(list.hashCode())
                            .checked(selectedList.equals(list))
                            .title(list)
                            .build());
        }
    }


    ArrayList<String> getListsAvailable() {
        try {
            listsAvailable = new getListsAvailableAsync().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return listsAvailable;
    }

    private class getListsAvailableAsync extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... arg0) {
            return listsAvailable = new SundtekJsonParser(getContext()).getListsAvailable();
        }

        @Override
        protected void onPostExecute(ArrayList<String> lists) {
            super.onPostExecute(listsAvailable);
        }
    }


    @Override
    public long onGuidedActionEditedAndProceed(GuidedAction action) {


        Log.d("TEST", "edit");

        return ACTION_ID_CURRENT;
    }


    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        if (listsAvailable.contains(action.getTitle())) {
            selectedList = action.getTitle().toString();
        }

        if (action.getId() == ACTION_ID_CONTINUE) {

            if(!selectedList.isEmpty()) {
                new SettingsHelper(getActivity())
                        .saveSelectedList(selectedList);
                Bundle args = new Bundle();
                args.putString(getContext().getString(R.string.active_list), selectedList);

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
