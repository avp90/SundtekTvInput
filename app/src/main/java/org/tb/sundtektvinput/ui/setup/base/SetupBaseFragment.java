package org.tb.sundtektvinput.ui.setup.base;

import android.support.v17.leanback.app.GuidedStepFragment;

import org.tb.sundtektvinput.R;

/**
 * Created on 07.03.2017.
 */

public class SetupBaseFragment extends GuidedStepFragment {
    protected final String breadcrumb = "Sundtek TvInput Setup";


    public int onProvideTheme() {
        return R.style.Theme_SetupWizard;
    }
}
