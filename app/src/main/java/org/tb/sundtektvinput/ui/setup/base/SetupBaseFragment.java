package org.tb.sundtektvinput.ui.setup.base;

import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.app.GuidedStepSupportFragment;

import org.tb.sundtektvinput.R;

/**
 * Created on 07.03.2017.
 */

public class SetupBaseFragment extends GuidedStepSupportFragment {
    protected final String breadcrumb = "Sundtek TvInput Setup";


    public int onProvideTheme() {
        return R.style.Theme_SetupWizard;
    }
}
