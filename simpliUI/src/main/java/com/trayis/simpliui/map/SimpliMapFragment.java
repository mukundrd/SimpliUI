package com.trayis.simpliui.map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.trayis.simpliui.R;
import com.trayis.simpliui.map.location.LocationTracker;
import com.trayis.simpliui.map.location.LocationUtils;
import com.trayis.simpliui.permission.OnDeniedPermissionCallback;
import com.trayis.simpliui.permission.PermissionCallback;
import com.trayis.simpliui.permission.PermissionHandler;
import com.trayis.simpliui.utils.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by mudesai on 12/6/17.
 */

public class SimpliMapFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener, PermissionCallback {

    private static final String TAG = "SimpliMapFragment";

    private static final int THRESHOLD = 2;

    private PermissionHandler mPermissionHandler;

    private BaseLocationTracker mTracker;

    private GoogleMap mMap;

    private int mZoom = -1;

    private int initialZoom = 13;

    private MapFragment mMapFragment;

    private boolean initializedByUser;

    private ImageView gpsLocationFix;

    private AutoCompleteTextView autoComplete;

    private SimpliLocationModel _locationModel, locationModel;

    private OnLocationChangeListener listener;

    private ImageView geoAutocompleteClear;

    private ImageView markerIcon;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_fragment_map, container, false);

        gpsLocationFix = view.findViewById(R.id.gps_location_fix);
        gpsLocationFix.setOnClickListener(this);

        markerIcon = view.findViewById(R.id.marker_icon_view);

        autoComplete = view.findViewById(R.id.autoComplete);
        geoAutocompleteClear = view.findViewById(R.id.geo_autocomplete_clear);

        autoComplete.setThreshold(THRESHOLD);
        autoComplete.setAdapter(new GeoAutoCompleteAdapter(getActivity(), new OnLocationChangeListener() {
            @Override
            public void onLocationChanged(SimpliLocationModel locationModel) {
                autoComplete.dismissDropDown();
                onNewLocation(locationModel.lattitude, locationModel.longitude, false);
            }
        })); // 'this' is Activity instance

        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
                autoComplete.setText(result.getAddress());
            }
        });

        autoComplete.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    geoAutocompleteClear.setVisibility(View.VISIBLE);
                } else {
                    geoAutocompleteClear.setVisibility(View.GONE);
                }
            }
        });

        geoAutocompleteClear.setOnClickListener(v -> autoComplete.setText(""));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        initMaps();

        if (locationModel == null) {
            requestPermission();
        } else {
            updateLocation();
        }
    }

    public void setInitialZoom(int initialZoom) {
        this.initialZoom = initialZoom;
    }

    public SimpliLocationModel getLocation() {
        return locationModel;
    }

    public ImageView getGpsLocationFixImageView() {
        return gpsLocationFix;
    }

    public AutoCompleteTextView getAutoCompleteTextView() {
        return autoComplete;
    }

    public ImageView getMarkerIcon() {
        return markerIcon;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.gps_location_fix) {
            Log.v(TAG, "GPS Loc Fix clicked, _locationModel is not null" + (_locationModel != null));
            if (_locationModel != null) {
                // mLatLang = null;
                updateLocationInternal(_locationModel.name, _locationModel.lattitude, _locationModel.longitude);
                updateLocation();
            }
            return;
        }
    }

    public void requestPermission() {

        OnDeniedPermissionCallback deniedPermissionCallback = OnDeniedPermissionCallback.Builder.with(getActivity().findViewById(android.R.id.content),
                R.string.geo_location_permission_denied_feedback)
                .withOpenSettingsButton(R.string.settings)
                .build();
        mPermissionHandler = new PermissionHandler.Builder(getActivity())
                .withDeniedCallback(deniedPermissionCallback)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withPermissionCallback(this)
                .build();

        mPermissionHandler.requestPermission();
    }

    public void permissionDenied() {
    }

    public void permissionGranted() {

        Activity activity = getActivity();
        if (activity == null || !isResumed()) {

        }

        if (!LocationUtils.isLocationEnabled(activity)) {
            LocationUtils.askEnableProviders(activity);
            return;
        }
        if (mTracker == null) {
            try {
                mTracker = new BaseLocationTracker(activity);
                mTracker.startListening();
            } catch (SecurityException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, int grantResult, String permission) {
        switch (grantResult) {
            case PERMISSION_DENIED:
                permissionDenied();
                break;
            case PERMISSION_ALREADY_AVAILABLE:
            case PERMISSION_GRANTED:
                permissionGranted();
                break;
            case SHOW_PERMISSION_RATIONALE:
                showPermissionRationale(this);
                break;
        }
    }

    @Override
    public int getRequestCode() {
        return Constants.Permissions.REQUEST_LOCATION_PERMISSION;
    }

    public void showPermissionRationale(PermissionCallback callback) {

        new AlertDialog.Builder(getActivity()).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.geo_location_permission_denied_feedback)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    permissionDenied();
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    mPermissionHandler.requestPermission(true);
                })
                .show();
    }

    private void initMaps() {
        if (mMapFragment == null) {
            mMapFragment = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map));
            mMapFragment.getMapAsync(this);
        }
    }

    protected void onNewLocation(Location location) {
        // mLatLang = null;
        onNewLocation(location.getLatitude(), location.getLongitude(), true);
    }

    public void updateLocation() {
        if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
            Log.v(TAG, "Fragment not resumed, returning");
            return;
        }

        if (mMap != null) {
            updateMap();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        updateMap();
    }

    private void updateMap() {
        if (locationModel != null) {
            LatLng latLngCenter = new LatLng(locationModel.lattitude, locationModel.longitude);
            if (mZoom == -1) {
                mZoom = initialZoom;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter, mZoom));
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngCenter));
            }
            if (listener != null) {
                listener.onLocationChanged(locationModel);
            }
            gpsLocationFix.setVisibility(View.VISIBLE);
            markerIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCameraIdle() {
        if (!initializedByUser) return;
        Projection projection = mMap != null ? mMap.getProjection() : null;
        if (projection != null) {
            LatLng mLatLang = projection.getVisibleRegion().latLngBounds.getCenter();
            onNewLocation(mLatLang.latitude, mLatLang.longitude, false);
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        initializedByUser = i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE;
    }

    private void onNewLocation(final double latitude, final double longitude, boolean isFromLocationListener) {
        Single.OnSubscribe<Boolean> subscribe = subscriber -> {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                if (isFromLocationListener) {
                    if (_locationModel == null) {
                        _locationModel = new SimpliLocationModel();
                    }
                    _locationModel.lattitude = latitude;
                    _locationModel.longitude = longitude;
                }

                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                boolean success = addresses != null && !addresses.isEmpty();
                if (success) {
                    Address address = addresses.get(0);
                    updateLocationInternal(address.getSubLocality(), latitude, longitude);
                }

                /*
                if (isFromLocationListener && mLatLang != null) {
                    return;
                }
                */

                subscriber.onSuccess(success);
            } catch (IOException e) {
                subscriber.onError(e);
            }
        };

        Single.create(subscribe).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleSubscriber<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
                if (value && locationModel != null) {
                    updateLocation();
                }
            }

            @Override
            public void onError(Throwable error) {

            }
        });
    }

    private void updateLocationInternal(String name, double latitude, double longitude) {
        if (locationModel == null) {
            Log.v(TAG, "updateLocationInternal new locationModel");
            locationModel = new SimpliLocationModel();
        }

        locationModel.name = name;
        locationModel.lattitude = latitude;
        locationModel.longitude = longitude;
    }

    public void setOnLocationChangeListener(OnLocationChangeListener listener) {
        this.listener = listener;
    }

    protected class BaseLocationTracker extends LocationTracker {

        public BaseLocationTracker(@NonNull Context context) throws SecurityException {
            super(context);
        }

        @Override
        public void onLocationFound(@NonNull Location location) {
            onNewLocation(location);
        }

        @Override
        public void onTimeout() {
            // TODO: What to do in this case?
            // showErrorSnackbar(getString(R.string.location_fetch_timedout));
            // locationPermissionDenied();
        }

    }

    public interface OnLocationChangeListener {
        void onLocationChanged(SimpliLocationModel locationModel);
    }
}
