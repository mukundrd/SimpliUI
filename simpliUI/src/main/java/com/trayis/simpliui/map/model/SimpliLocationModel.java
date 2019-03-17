package com.trayis.simpliui.map.model;

/**
 * Created by mudesai on 12/6/17.
 */

public class SimpliLocationModel {

    public double lattitude, longitude;

    public String name;
    public String city;

    @Override
    public String toString() {
        return "SimpliLocationModel { lattitude=" + lattitude + ", longitude=" + longitude + ", name='" + name + "', city='" + city + "' }";
    }
}
