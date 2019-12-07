package org.tb.sundtektvinput.ui.setup;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;

import org.tb.sundtektvinput.R;

/**
 * Created by thomas on 03/09/16.
 */
public class SetupActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_layout);
        GuidedStepSupportFragment.addAsRoot(this, new IpAddressInputFragment(), android.R.id.content);
    }

    @Override
    public void onBackPressed() {
        if (!(GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(getSupportFragmentManager()) instanceof EpgScanFragment))
            super.onBackPressed();
    }
}
