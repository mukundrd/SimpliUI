package com.trayis.simpliui.map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.trayis.simpliui.R;
import com.trayis.simpliui.map.api.APIClient;
import com.trayis.simpliui.map.location.LocationTracker;
import com.trayis.simpliui.map.location.LocationUtils;
import com.trayis.simpliui.map.location.ProviderException;
import com.trayis.simpliui.map.model.Place;
import com.trayis.simpliui.map.model.PlacesDetails;
import com.trayis.simpliui.map.model.PlacesPrediction;
import com.trayis.simpliui.map.model.SimpliLocationModel;
import com.trayis.simpliui.permission.OnDeniedPermissionCallback;
import com.trayis.simpliui.permission.PermissionCallback;
import com.trayis.simpliui.permission.PermissionHandler;
import com.trayis.simpliui.utils.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;

/**
 * Created by mudesai on 12/6/17.
 */

public class SimpliMapFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener, PermissionCallback {

    private static final String TAG = "SimpliMapFragment";

    private static final int THRESHOLD = 2;

    private PermissionHandler mPermissionHandler;

    private BaseLocationTracker mTracker;

    private GoogleMap mMap;

    private MapView mMapView;

    private DelayAutoCompleteTextView autoComplete;

    private SimpliLocationModel _locationModel, locationModel;

    private OnLocationChangeListener listener;

    private ImageView geoAutocompleteClear, markerIcon, gpsLocationFix;

    private String apiKey;

    private int mZoom = -1, initialZoom = 15;

    private boolean shouldRequestOnResume = true, mInitializedByUser, mFromSelection, mMoveIdle = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_fragment_map, container, false);

        FragmentActivity activity = getActivity();

        // Gets the MapView from the XML layout and creates it
        mMapView = view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMapView.getMapAsync(this);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(activity);

        gpsLocationFix = view.findViewById(R.id.gps_location_fix);
        gpsLocationFix.setOnClickListener(this);

        markerIcon = view.findViewById(R.id.marker_icon_view);

        autoComplete = view.findViewById(R.id.autoComplete);
        geoAutocompleteClear = view.findViewById(R.id.geo_autocomplete_clear);

        try {
            ApplicationInfo app = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            apiKey = bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        final APIClient.ApiInterface apiService = APIClient.getClient().create(APIClient.ApiInterface.class);

        GeoAutoCompleteAdapter adapter = new GeoAutoCompleteAdapter(activity, new LocationServiceCallback() {
            @Override
            public void onLocationChanged(SimpliLocationModel locationModel) {
                autoComplete.dismissDropDown();
                autoComplete.setText(locationModel.name, false);
                setLocationModel(locationModel);
            }

            @Override
            public void locationChangeError() {
            }

            @Override
            public Call<PlacesPrediction> getNewPlacesList(String place) {
                if (_locationModel != null) {
                    return apiService.getPlaces(_locationModel.lattitude + "," + _locationModel.longitude, place, apiKey);
                }
                return apiService.getPlaces(place, apiKey);
            }

            @Override
            public Call<PlacesDetails> getPlaceDetails(String placeId) {
                return apiService.getPlaceDetails(placeId, apiKey);
            }
        });

        autoComplete.setThreshold(THRESHOLD);
        autoComplete.setAdapter(adapter);
        autoComplete.setOnItemClickListener((adapterView, view1, position, id) -> {
            if (!isValidFragmentState()) return;
            Object itemAtPosition = adapterView.getItemAtPosition(position);
            if (itemAtPosition instanceof GeoSearchResult) {
                GeoSearchResult result = (GeoSearchResult) itemAtPosition;
                autoComplete.setText(result.name);
            } else if (itemAtPosition instanceof Place) {
                Place place = (Place) itemAtPosition;
                autoComplete.setText(place.structured_formatting.main_text);
            }
            adapter.onResultSelected(view1);
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
                if (!isValidFragmentState()) return;
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

    public void setLocationModel(SimpliLocationModel locationModel) {
        mFromSelection = true;
        onNewLocation(locationModel.lattitude, locationModel.longitude, false, locationModel.name);
    }

    private boolean isValidFragmentState() {
        return !(getActivity() == null || isRemoving() || isDetached() || isHidden());
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();

        if (locationModel == null && shouldRequestOnResume) {
            requestPermission();
        } else {
            updateLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
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
        if (!isValidFragmentState()) return;
        if (v.getId() == R.id.gps_location_fix) {
            if (mPermissionHandler.checkIfPermissionIsAvailable(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.v(TAG, "GPS Loc Fix clicked, _locationModel is not null" + (_locationModel != null));
                if (_locationModel != null) {
                    // mLatLang = null;
                    mFromSelection = false;
                    updateLocationInternal(_locationModel.name, _locationModel.lattitude, _locationModel.longitude);
                    updateLocation();
                }
            }
            requestPermission();
        }
    }

    public void requestPermission() {
        if (!isValidFragmentState()) return;
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
        if (!isValidFragmentState()) return;
        Activity activity = getActivity();
        if (activity == null) {
            return;
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
        if (!isValidFragmentState()) return;
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
        if (!isValidFragmentState()) return;

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

    private void updateLocation() {
        if (!isValidFragmentState()) return;
        /*if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
            Logging.v(TAG, "Fragment not resumed, returning");
            return;
        }*/

        if (mMap != null) {
            updateMap();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!isValidFragmentState()) return;
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraIdleListener(this);
        updateMap();
    }

    private synchronized void updateMap() {
        if (locationModel != null && mMoveIdle) {
            autoComplete.setThreshold(Integer.MAX_VALUE);
            autoComplete.setText(locationModel.name);
            autoComplete.setThreshold(1);
            autoComplete.dismissDropDown();
            LatLng latLngCenter = new LatLng(locationModel.lattitude, locationModel.longitude);
            Log.v(TAG, "Zoom : " + mZoom + ", Location : " + locationModel.toString());
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
        mMoveIdle = true;
        if (!isValidFragmentState() || !mInitializedByUser) return;
        Projection projection = mMap != null ? mMap.getProjection() : null;
        if (projection != null) {
            LatLng mLatLang = projection.getVisibleRegion().latLngBounds.getCenter();
            onNewLocation(mLatLang.latitude, mLatLang.longitude, false, null);
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        mMoveIdle = false;
        Log.v(TAG, "Zoom Camera Move : " + i);
        if (!isValidFragmentState()) return;
        mInitializedByUser = i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE;
    }

    private void onNewLocation(final double latitude, final double longitude, boolean isFromLocationListener, String name) {
        if (!isValidFragmentState()) return;
        SingleOnSubscribe<Boolean> subscribe = subscriber -> {
            try {
                if (isFromLocationListener) {
                    if (_locationModel == null) {
                        _locationModel = new SimpliLocationModel();
                    }
                    _locationModel.lattitude = latitude;
                    _locationModel.longitude = longitude;
                }

                if (isFromLocationListener && (mInitializedByUser || mFromSelection)) return;

                boolean success;
                if (name != null) {
                    success = true;
                    updateLocationInternal(name, latitude, longitude);
                } else {
                    Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    success = addresses != null && !addresses.isEmpty();
                    if (success) {
                        Address address = addresses.get(0);
                        StringBuilder nameBuilder = new StringBuilder();
                        String featureName = address.getFeatureName();
                        if (!TextUtils.isEmpty(featureName)) {
                            nameBuilder.append(featureName + ", ");
                        }
                        String subLocality = address.getSubLocality();
                        if (!TextUtils.isEmpty(subLocality)) {
                            nameBuilder.append(subLocality);
                        }
                        if (isFromLocationListener) {
                            _locationModel.name = nameBuilder.toString();
                        }
                        updateLocationInternal(nameBuilder.toString(), latitude, longitude);
                    }
                }
                subscriber.onSuccess(success);
            } catch (IOException e) {
                subscriber.onError(e);
            }
        };

        Single.create(subscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(Boolean value) {
                        if (value && SimpliMapFragment.this.locationModel != null) {
                            updateLocation();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                });
    }

    private void updateLocationInternal(String name, double latitude, double longitude) {
        if (!isValidFragmentState()) return;
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

    public void requestOnResume(boolean shoulrRequestOnResume) {
        this.shouldRequestOnResume = shoulrRequestOnResume;
    }

    protected class BaseLocationTracker extends LocationTracker {

        public BaseLocationTracker(@NonNull Context context) throws SecurityException {
            super(context);
        }

        @Override
        public void onLocationFound(@NonNull Location location) {
            onNewLocation(location.getLatitude(), location.getLongitude(), true, null);
        }

        @Override
        public void onTimeout() {
            // TODO: What to do in this case?
            // showErrorSnackbar(getString(R.string.location_fetch_timedout));
            // locationPermissionDenied();
        }

        @Override
        public void onProviderError(@NonNull ProviderException providerError) {
            if (LocationManager.GPS_PROVIDER.equals(providerError.getProvider()) && isValidFragmentState()) {
                LocationUtils.askEnableProviders(getActivity());
            }
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

    }

    public interface OnLocationChangeListener {
        void onLocationChanged(SimpliLocationModel locationModel);

        void locationChangeError();
    }

    public interface LocationServiceCallback {

        void onLocationChanged(SimpliLocationModel locationModel);

        void locationChangeError();

        Call<PlacesPrediction> getNewPlacesList(String place) throws IOException;

        Call<PlacesDetails> getPlaceDetails(String placeId);
    }
}
