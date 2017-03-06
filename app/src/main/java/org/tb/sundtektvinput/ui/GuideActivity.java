package org.tb.sundtektvinput.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;

import org.tb.sundtektvinput.R;


/**
 * Created by thomas on 03/09/16.
 */
public class GuideActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_layout);
        GuidedStepFragment.add(getFragmentManager(), new GuideFristFragment());
    }
}
