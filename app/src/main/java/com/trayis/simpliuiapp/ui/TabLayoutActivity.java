package com.trayis.simpliuiapp.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.trayis.simpliui.tablayout.SimpliTabLayout;
import com.trayis.simpliuiapp.R;

public class TabLayoutActivity extends AppCompatActivity implements SimpliTabLayout.OnTabSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_layout);

        ViewPager mViewPager = findViewById(R.id.container);
        TabLayoutAdapter mAdapter = new TabLayoutAdapter();
        mViewPager.setAdapter(mAdapter);
        SimpliTabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(SimpliTabLayout.MODE_FIXED);

        tabLayout.setSelectedTabIndicatorHeight(4);

        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());

        //set drawables for each tab
        tabLayout.getTabAt(1).setIcon(R.drawable.task_tab_drawable);
        tabLayout.getTabAt(2).setIcon(R.drawable.board_tab_drawable);
        tabLayout.getTabAt(3).setIcon(R.drawable.team_tab_drawable);
        tabLayout.getTabAt(4).setIcon(R.drawable.settings_tab_drawable);

        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public boolean isTabSelectionNotAllowed(SimpliTabLayout.Tab tab) {
        return tab.getPosition() == 2;
    }

    @Override
    public void onTabSelected(SimpliTabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(SimpliTabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(SimpliTabLayout.Tab tab) {

    }

    class TabLayoutAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return false;
        }
    }
}
