package com.trayis.simpliui.map.model;

/**
 * Created by mudesai on 1/13/18.
 */

public class PlacesDetails {
    public Result result;

    public String[] html_attributions;

    public String status;

    @Override
    public String toString() {
        return "PlacesDetails [result = " + result + ", html_attributions = " + html_attributions + ", status = " + status + "]";
    }
}
