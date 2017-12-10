package com.trayis.simpliuieg.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.trayis.simpliui.map.SimpliLocationModel;
import com.trayis.simpliui.map.SimpliMapFragment;
import com.trayis.simpliuieg.R;

/**
 * Created by mudesai on 12/6/17.
 */

public class LocationActivity extends AppCompatActivity implements SimpliMapFragment.OnLocationChangeListener {

    private SimpliMapFragment mMapFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMapFragment = (SimpliMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_fragment);
        if (mMapFragment != null) {
            mMapFragment.setOnLocationChangeListener(this);
            mMapFragment.getLocation();
        }
    }

    @Override
    public void onLocationChanged(SimpliLocationModel locationModel) {
        Log.v("LocationActivity", locationModel.toString());
    }
}
