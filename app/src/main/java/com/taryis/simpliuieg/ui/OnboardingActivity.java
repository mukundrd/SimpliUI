package com.taryis.simpliuieg.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.taryis.simpliui.onboarding.OnboardingView;
import com.taryis.simpliuieg.R;

/**
 * Created by Mukund on 16-07-2017.
 */

public class OnboardingActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        OnboardingView view = findViewById(R.id.onboarding_view);
        view.focusForViewWithPath(getString(R.string.svg_path), findViewById(R.id.data), null, OnboardingView.BubblePosition.BELOW_NO_CARAT, "This is content", 10);
    }

}
