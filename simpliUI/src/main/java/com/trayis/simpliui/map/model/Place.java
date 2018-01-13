package com.trayis.simpliui.map.model;

/**
 * Created by mudesai on 1/13/18.
 */

public class Place {

    public String id;

    public String place_id;

    public FormattedText structured_formatting;

    public MatchedText[] matched_substrings;

    public String description;

    public Terms[] terms;

    public String[] types;

    public String reference;

    @Override
    public String toString() {
        return "Place [id = " + id + ", place_id = " + place_id + ", structured_formatting = " + structured_formatting + ", matched_substrings = " + matched_substrings + ", description = " + description + ", terms = " + terms + ", types = " + types + ", reference = " + reference + "]";
    }
}
