package com.trayis.simpliui.map;

/**
 * Created by mudesai on 12/7/17.
 */

public class GeoSearchResult {

    public CharSequence name;

    public String placeId;

    public double lattitude, longitude;

    public GeoSearchResult(CharSequence name, String placeId) {
        this.name = name;
        this.placeId = placeId;
    }

    @Override
    public String toString() {
        return "GeoSearchResult{" +
                "name ='" + name + '\'' +
                ", placeId ='" + placeId +
                ", lattitude ='" + lattitude + '\'' +
                ", longitude ='" + longitude + '\'' +
                '}';
    }
}
