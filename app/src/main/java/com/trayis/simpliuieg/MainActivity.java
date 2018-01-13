package com.trayis.simpliuieg;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.trayis.simpliuieg.ui.LocationActivity;
import com.trayis.simpliuieg.ui.OnboardingActivity;
import com.trayis.simpliuieg.ui.TabLayoutActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tab_layout).setOnClickListener(this);
        findViewById(R.id.onboarding).setOnClickListener(this);
        findViewById(R.id.location).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_layout:
                startActivity(new Intent(this, TabLayoutActivity.class));
                break;
            case R.id.onboarding:
                startActivity(new Intent(this, OnboardingActivity.class));
                break;
            case R.id.location:
                startActivity(new Intent(this, LocationActivity.class));
                break;
        }
    }
}
