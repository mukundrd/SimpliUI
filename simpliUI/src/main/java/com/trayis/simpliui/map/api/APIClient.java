package com.trayis.simpliui.map.api;

import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.trayis.simpliui.map.model.PlacesDetails;
import com.trayis.simpliui.map.model.PlacesPrediction;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mudesai on 1/13/18.
 */

public class APIClient {

    private static final String baseUrl = "https://maps.googleapis.com/maps/api/";

    public static Retrofit getClient(Context context) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS);

        if (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        OkHttpClient client = builder.build();
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public interface ApiInterface {

        @GET("place/autocomplete/json?")
        Call<PlacesPrediction> getPlaces(@Query(value = "input", encoded = true) String input, @Query(value = "key", encoded = true) String key);

        @GET("place/autocomplete/json?")
        Call<PlacesPrediction> getPlaces(@Query(value = "location", encoded = true) String location, @Query(value = "input", encoded = true) String input, @Query(value = "key", encoded = true) String key);

        @GET("place/details/json?")
        Call<PlacesDetails> getPlaceDetails(@Query(value = "placeid", encoded = true) String placeid, @Query(value = "key", encoded = true) String key);
    }

}
