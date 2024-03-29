package com.trayis.simpliui.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.trayis.simpliui.map.model.AddressComplent;
import com.trayis.simpliui.map.model.Geometry;
import com.trayis.simpliui.map.model.Location;
import com.trayis.simpliui.map.model.Place;
import com.trayis.simpliui.map.model.PlacesDetails;
import com.trayis.simpliui.map.model.PlacesPrediction;
import com.trayis.simpliui.map.model.Result;
import com.trayis.simpliui.map.model.SimpliLocationModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by mudesai on 12/7/17.
 */

class GeoAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private SimpliMapFragment.LocationServiceCallback mLocationServiceCallback;

    private Context mContext;

    private ArrayList<Place> resultList = new ArrayList<>();

    GeoAutoCompleteAdapter(Context context, SimpliMapFragment.LocationServiceCallback callback) {
        mContext = context;
        mLocationServiceCallback = callback;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Place getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                convertView = inflater.inflate(com.trayis.simpliui.R.layout.geo_search_result_item, parent, false);
            }
        }

        Place item = getItem(position);
        convertView.setTag(item);
        ((TextView) convertView.findViewById(com.trayis.simpliui.R.id.geo_search_primary_text)).setText(item.structured_formatting.main_text);
        ((TextView) convertView.findViewById(com.trayis.simpliui.R.id.geo_search_secondary_text)).setText(item.structured_formatting.secondary_text);

        return convertView;
    }

    void onResultSelected(View v) {
        final Place tag = (Place) v.getTag();
        Call<PlacesDetails> placeDetailsCall = mLocationServiceCallback.getPlaceDetails(tag.place_id);
        placeDetailsCall.enqueue(new Callback<PlacesDetails>() {
            @Override
            public void onResponse(Call<PlacesDetails> call, Response<PlacesDetails> response) {
                if (response.isSuccessful()) {
                    PlacesDetails placesDetails = response.body();
                    if (placesDetails != null) {
                        Result result = placesDetails.result;
                        if (result != null) {
                            Geometry geometry = result.geometry;
                            if (geometry != null) {
                                Location location = geometry.location;
                                if (location != null) {
                                    SimpliLocationModel locationModel = new SimpliLocationModel();
                                    locationModel.lattitude = location.lat;
                                    locationModel.longitude = location.lng;
                                    locationModel.name = tag.structured_formatting.main_text;
                                    locationModel.city = getCity(result.address_components);
                                    mLocationServiceCallback.onLocationChanged(locationModel);
                                    return;
                                }
                            }
                        }
                    }
                }
                mLocationServiceCallback.locationChangeError();
            }

            @Override
            public void onFailure(Call<PlacesDetails> call, Throwable t) {
            }
        });
    }

    private String getCity(AddressComplent[] addressComponents) {
        if (addressComponents != null && addressComponents.length > 0) {
            for (AddressComplent addressComplent : addressComponents) {
                if (addressComplent == null) continue;
                String[] types = addressComplent.types;
                if (types != null) {
                    for (String type : types) {
                        if ("locality".equalsIgnoreCase(type))
                            return addressComplent.long_name;
                    }
                }
            }
        }
        return "CITY";
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Place> locations = new ArrayList<>();
                    try {
                        Call<PlacesPrediction> places = mLocationServiceCallback.getNewPlacesList(constraint.toString());
                        Response<PlacesPrediction> execute = places.execute();
                        PlacesPrediction body = execute.body();
                        Place[] result = new Place[0];
                        if (body != null) {
                            result = body.predictions;
                        }
                        locations.addAll(Arrays.asList(result));
                    } catch (IOException ignored) {
                    }

                    filterResults.values = locations;
                    filterResults.count = locations.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList.clear();
                    resultList.addAll((List) results.values);
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    public void setLocationServiceCallback(SimpliMapFragment.LocationServiceCallback locationServiceCallback) {
        mLocationServiceCallback = locationServiceCallback;
    }
}
