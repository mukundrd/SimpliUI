package com.trayis.simpliui.map.location;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import android.util.Log;

import com.trayis.simpliui.BuildConfig;

/**
 * Created by mukundrd on 6/9/17.
 */

public abstract class LocationTracker implements LocationListener {

    private static final String TAG = "LocationTracker";

    private static Location sLocation;

    private LocationManager mLocationManagerService;
    private volatile boolean mIsListening = false;
    private volatile boolean mIsLocationFound = false;

    private Context mContext;

    private long sLastUpdateTime = -1;

    private TrackerSettings mTrackerSettings = TrackerSettings.DEFAULT;

    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public LocationTracker(@NonNull Context context) throws SecurityException {
        this.mContext = context;

        // Get the location manager
        this.mLocationManagerService = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // default
        if (sLocation == null) {
            if (mTrackerSettings.shouldUseGPS()) {
                LocationTracker.sLocation = mLocationManagerService.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (mTrackerSettings.shouldUseNetwork()) {
                LocationTracker.sLocation = mLocationManagerService.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (mTrackerSettings.shouldUsePassive()) {
                LocationTracker.sLocation = mLocationManagerService.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        }
    }

    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public final synchronized void startListening() throws SecurityException {
        if (mIsListening) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Relax, LocationTracked is already listening for location updates");
            }
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LocationTracked is now listening for location updates");
        }

        long timeBetweenUpdates = mTrackerSettings.getTimeBetweenUpdates();
        float metersBetweenUpdates = mTrackerSettings.getMetersBetweenUpdates();

        if (mTrackerSettings.shouldUseGPS()) {
            if (LocationUtils.isGpsProviderEnabled(mContext)) {
                mLocationManagerService.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeBetweenUpdates, metersBetweenUpdates, this);
            } else {
                onProviderError(new ProviderException(LocationManager.GPS_PROVIDER, "Provider is not enabled"));
            }
        }

        if (mTrackerSettings.shouldUseNetwork()) {
            if (LocationUtils.isNetworkProviderEnabled(mContext)) {
                mLocationManagerService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeBetweenUpdates, metersBetweenUpdates, this);
            } else {
                onProviderError(new ProviderException(LocationManager.NETWORK_PROVIDER, "Provider is not enabled"));
            }
        }

        if (mTrackerSettings.shouldUsePassive()) {
            if (LocationUtils.isPassiveProviderEnabled(mContext)) {
                mLocationManagerService.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, timeBetweenUpdates, metersBetweenUpdates, this);
            } else {
                onProviderError(new ProviderException(LocationManager.PASSIVE_PROVIDER, "Provider is not enabled"));
            }
        }
        mIsListening = true;
        quickFix();

        if (mTrackerSettings.getTimeout() != -1) {
            new Handler().postDelayed(() -> {
                if (!mIsLocationFound && mIsListening) {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "No location found in the meantime");
                    }
                    stopListening();
                    onTimeout();
                }
            }, mTrackerSettings.getTimeout());
        }
    }

    public final synchronized void stopListening() {
        if (mIsListening) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "LocationTracked wasn't listening for location updates anyway");
            }
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LocationTracked has stopped listening for location updates");
        }
        mLocationManagerService.removeUpdates(this);
        mIsListening = false;
    }

    public final void quickFix() {
        if (LocationTracker.sLocation != null && isWithinAMinute()) {
            onLocationChanged(LocationTracker.sLocation);
        }
    }

    private boolean isWithinAMinute() {
        return (System.currentTimeMillis() - sLastUpdateTime) < 60 * 1000;
    }

    public final boolean isListening() {
        return mIsListening;
    }

    @Override
    public final void onLocationChanged(@NonNull Location location) {
        Log.i(TAG, "Location has changed, new location is " + location);

        sLocation = new Location(location);
        sLastUpdateTime = System.currentTimeMillis();

        mIsLocationFound = true;
        onLocationFound(location);
    }

    public void onProviderError(@NonNull ProviderException providerError) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Provider (" + providerError.getProvider() + ")", providerError);
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Provider (" + provider + ") has been enabled");
        }
    }

    @Override
    public void onStatusChanged(@NonNull String provider, int status, Bundle extras) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Provider (" + provider + ") status has changed, new status is " + status);
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Provider (" + provider + ") has been disabled");
        }
    }

    public abstract void onLocationFound(@NonNull Location location);

    public abstract void onTimeout();

}
