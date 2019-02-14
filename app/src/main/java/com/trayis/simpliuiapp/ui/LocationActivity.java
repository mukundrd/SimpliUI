package com.trayis.simpliuiapp.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.trayis.simpliui.map.SimpliMapFragment;
import com.trayis.simpliui.map.model.SimpliLocationModel;
import com.trayis.simpliuiapp.R;

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
            mMapFragment.setApiKey(getString(R.string.google_geo_id));
            mMapFragment.getLocation();
        }
    }

    @Override
    public void onLocationChanged(SimpliLocationModel locationModel) {
        Log.v("LocationActivity", locationModel.toString());
    }

    @Override
    public void locationChangeError() {

    }
}
