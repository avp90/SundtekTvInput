package org.tb.sundtektvinput.ui.setup;

import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.widget.Toast;

import org.tb.sundtektvinput.R;

import java.util.List;

public class GuideFifthFragment extends GuideBaseFragment {


    @Override
    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Scan finished";
        String description = "You are now ready to watch TV";
        Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(0)
                .title("Finish")
                .build());
    }


    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        if (action.getId() == 0) {
            Toast.makeText(getActivity().getApplicationContext(), "NEXT", Toast.LENGTH_LONG).show();
            getActivity().finish();
            finishGuidedStepFragments();

        }

    }





}
