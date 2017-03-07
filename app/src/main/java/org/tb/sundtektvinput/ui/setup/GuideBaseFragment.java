package org.tb.sundtektvinput.ui.setup;

import android.support.v17.leanback.app.GuidedStepFragment;

import org.tb.sundtektvinput.R;

/**
 * Created on 07.03.2017.
 */

public class GuideBaseFragment extends GuidedStepFragment {
    String breadcrumb = "Sundtek TvInput Setup";


    public int onProvideTheme() {
        return R.style.Theme_SetupWizard;
    }
}
