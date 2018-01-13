package com.trayis.simpliui.map.model;

/**
 * Created by mudesai on 1/13/18.
 */

public class FormattedText {

    public String secondary_text;

    public String main_text;

    public MainMatchedString[] main_text_matched_substrings;

    @Override
    public String toString() {
        return "ClassPojo [secondary_text = " + secondary_text + ", main_text = " + main_text + ", main_text_matched_substrings = " + main_text_matched_substrings + "]";
    }
}
