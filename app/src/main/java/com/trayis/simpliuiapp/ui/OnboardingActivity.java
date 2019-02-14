package com.trayis.simpliuiapp.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.trayis.simpliui.onboarding.OnboardingView;
import com.trayis.simpliuiapp.R;

/**
 * Created by Mukund on 16-07-2017.
 */

public class OnboardingActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        OnboardingView view = findViewById(R.id.onboarding_view);
        view.focusForView(getString(R.string.svg_path), findViewById(R.id.heart), null, OnboardingView.BubblePosition.ABOVE_NO_CARAT, "This is content", 5);
    }

}
