package com.taryis.examples.bottomtab1;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.taryis.simpliui.SimpliTabLayout;

public class MainActivity extends AppCompatActivity {

    private MainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mAdapter = new MainAdapter();
        mViewPager.setAdapter(mAdapter);
        SimpliTabLayout tabLayout = (SimpliTabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(SimpliTabLayout.MODE_FIXED);

        tabLayout.setSelectedTabIndicatorHeight(4);

        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());

        //set drawables for each tab
        tabLayout.getTabAt(0).setIcon(R.drawable.files_tab_drawable);
        tabLayout.getTabAt(1).setIcon(R.drawable.task_tab_drawable);
        tabLayout.getTabAt(2).setIcon(R.drawable.board_tab_drawable);
        tabLayout.getTabAt(3).setIcon(R.drawable.team_tab_drawable);
        tabLayout.getTabAt(4).setIcon(R.drawable.settings_tab_drawable);
    }
}
