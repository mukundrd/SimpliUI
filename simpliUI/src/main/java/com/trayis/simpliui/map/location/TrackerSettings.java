package com.trayis.simpliui.map.location;

/**
 * Created by mukundrd on 6/9/17.
 */

class TrackerSettings {

    public static final TrackerSettings DEFAULT = new TrackerSettings();

    private static final int ONE_MIN = 20 * 1000;
    public static final long DEFAULT_MIN_TIME_BETWEEN_UPDATES = ONE_MIN;
    public static final int DEFAULT_TIMEOUT = 5 * ONE_MIN;

    public static final float DEFAULT_MIN_METERS_BETWEEN_UPDATES = 25;

    private long mTimeBetweenUpdates = DEFAULT_MIN_TIME_BETWEEN_UPDATES;
    private float mMetersBetweenUpdates = DEFAULT_MIN_METERS_BETWEEN_UPDATES;
    private int mTimeout = DEFAULT_TIMEOUT;

    private boolean mUseGPS = true;
    private boolean mUseNetwork = true;
    private boolean mUsePassive = false;

    public long getTimeBetweenUpdates() {
        return mTimeBetweenUpdates;
    }

    public float getMetersBetweenUpdates() {
        return mMetersBetweenUpdates;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public boolean shouldUseGPS() {
        return mUseGPS;
    }

    public boolean shouldUseNetwork() {
        return mUseNetwork;
    }

    public boolean shouldUsePassive() {
        return mUsePassive;
    }

}
