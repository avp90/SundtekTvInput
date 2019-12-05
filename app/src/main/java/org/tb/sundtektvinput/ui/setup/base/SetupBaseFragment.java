package org.tb.sundtektvinput.ui.setup.base;

import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.app.GuidedStepSupportFragment;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.SundtekTvInputApp;

/**
 * Created on 07.03.2017.
 */

public class SetupBaseFragment extends GuidedStepSupportFragment {
    protected final String breadcrumb = "Sundtek TvInput Setup";

    protected SundtekTvInputApp getApp() {
        return (SundtekTvInputApp)getActivity().getApplication();
    }

    public int onProvideTheme() {
        return R.style.Theme_SetupWizard;
    }
}
