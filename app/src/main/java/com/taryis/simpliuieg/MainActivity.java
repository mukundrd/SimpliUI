package com.taryis.simpliuieg;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.taryis.simpliuieg.ui.OnboardingActivity;
import com.taryis.simpliuieg.ui.TabLayoutActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tab_layout).setOnClickListener(this);
        findViewById(R.id.onboarding).setOnClickListener(this);
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
        }
    }
}