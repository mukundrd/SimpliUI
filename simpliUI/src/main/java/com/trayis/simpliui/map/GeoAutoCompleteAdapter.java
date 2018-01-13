package com.trayis.simpliui.map;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.trayis.simpliui.R;
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

public class GeoAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private SimpliMapFragment.LocationServiceCallback mLocationServiceCallback;

    private Context mContext;

    private List<Place> resultList = new ArrayList();

    public GeoAutoCompleteAdapter(Context context, SimpliMapFragment.LocationServiceCallback callback) {
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
            convertView = inflater.inflate(R.layout.geo_search_result_item, parent, false);
        }

        Place item = getItem(position);
        convertView.setTag(item);
        ((TextView) convertView.findViewById(R.id.geo_search_primary_text)).setText(item.structured_formatting.main_text);
        ((TextView) convertView.findViewById(R.id.geo_search_secondary_text)).setText(item.structured_formatting.secondary_text);
        convertView.setOnClickListener(v -> {
            Place tag = (Place) v.getTag();
            Call<PlacesDetails> placeDetails = mLocationServiceCallback.getPlaceDetails(tag.place_id);
            placeDetails.enqueue(new Callback<PlacesDetails>() {
                @Override
                public void onResponse(Call<PlacesDetails> call, Response<PlacesDetails> response) {
                    if (response.isSuccessful()) {
                        PlacesDetails placesDetails = response.body();
                        if (placeDetails != null) {
                            Result result = placesDetails.result;
                            if (result != null) {
                                Geometry geometry = result.geometry;
                                if (geometry != null) {
                                    Location location = geometry.location;
                                    if (location != null) {
                                        SimpliLocationModel locationModel = new SimpliLocationModel();
                                        locationModel.lattitude = location.lat;
                                        locationModel.longitude = location.lng;
                                        locationModel.name = item.structured_formatting.main_text;
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
        });

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Place> locations = new ArrayList<>();
                    try {
                        Call<PlacesPrediction> places = mLocationServiceCallback.getNewPlacesList(constraint.toString());
                        Response<PlacesPrediction> execute = places.execute();
                        PlacesPrediction body = execute.body();
                        Place[] result = body.predictions;
                        locations.addAll(Arrays.asList(result));
                    } catch (IOException e) {
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
        return filter;
    }

    public void setLocationServiceCallback(SimpliMapFragment.LocationServiceCallback locationServiceCallback) {
        mLocationServiceCallback = locationServiceCallback;
    }
}
