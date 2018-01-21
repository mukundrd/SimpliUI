package com.trayis.simpliui.map.model;

/**
 * Created by mudesai on 1/13/18.
 */

public class PlacesPrediction {

    public Place[] predictions;

    public String status;

    @Override
    public String toString() {
        return "PlacesPrediction [predictions = " + predictions + ", status = " + status + "]";
    }
}
