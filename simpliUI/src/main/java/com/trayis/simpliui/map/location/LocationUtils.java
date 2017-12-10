package com.trayis.simpliui.map.location;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.trayis.simplimvp.BuildConfig;
import com.trayis.simplimvp.utils.Logging;
import com.trayis.simpliui.R;

/**
 * Created by mukundrd on 6/9/17.
 */

public class LocationUtils {

    private static final String TAG = "LocationUtils";

    private static TrackerSettings sTrackerSettings = TrackerSettings.DEFAULT;

    public static boolean isGpsProviderEnabled(@NonNull Context context) {
        return isProviderEnabled(context, LocationManager.GPS_PROVIDER);
    }

    public static boolean isNetworkProviderEnabled(@NonNull Context context) {
        return isProviderEnabled(context, LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isPassiveProviderEnabled(@NonNull Context context) {
        return isProviderEnabled(context, LocationManager.PASSIVE_PROVIDER);
    }

    private static boolean isProviderEnabled(@NonNull Context context, @NonNull String provider) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = manager.isProviderEnabled(provider);
        if (BuildConfig.DEBUG) {
            Logging.d(TAG, String.format("%s %b", provider, enabled));
        }
        return enabled;
    }

    public static boolean isLocationEnabled(@NonNull Context context) {
        return (sTrackerSettings.shouldUseGPS() && isGpsProviderEnabled(context)) ||
               (sTrackerSettings.shouldUseNetwork() && isNetworkProviderEnabled(context)) ||
               (sTrackerSettings.shouldUsePassive() && isPassiveProviderEnabled(context));
    }

    public static void askEnableProviders(@NonNull final Context context) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.location_enable_message)
                .setCancelable(false)
                .setPositiveButton(R.string.provider_settings_yes, (dialog, which) -> context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton(R.string.provider_settings_no, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
