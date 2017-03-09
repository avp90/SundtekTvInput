package org.tb.sundtektvinput.ui.setup;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.widget.Toast;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.ui.setup.base.SetupBaseFragment;

import java.util.List;

public class FinishFragment extends SetupBaseFragment {


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
        actions.add(new GuidedAction.Builder(getActivity())
                .id(0)
                .title("Finish")
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == 0) {
            Toast.makeText(getActivity(), "NEXT", Toast.LENGTH_LONG).show();
            finishGuidedStepFragments();
        }
    }
}
