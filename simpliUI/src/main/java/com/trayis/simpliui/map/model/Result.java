package com.trayis.simpliui.map.model;

/**
 * Created by mudesai on 1/13/18.
 */

public class Result {

    public String icon;

    public String place_id;

    public Geometry geometry;

    public String id;

    public String name;

    @Override
    public String toString() {
        return "Result [icon = " + icon + ", place_id = " + place_id + ", geometry = " + geometry + ", name = " + name + "]";
    }
}
