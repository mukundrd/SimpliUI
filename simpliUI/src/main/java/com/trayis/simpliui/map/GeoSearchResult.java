package com.trayis.simpliui.map;

import android.location.Address;

/**
 * Created by mudesai on 12/7/17.
 */

public class GeoSearchResult {

    public Address address;

    public GeoSearchResult(Address address) {
        this.address = address;
    }

    public String getAddress() {

        String displayAddress = "";

        displayAddress += address.getAddressLine(0) + "\n";

        for (int i = 1; i < address.getMaxAddressLineIndex(); i++) {
            displayAddress += address.getAddressLine(i) + ", ";
        }

        displayAddress = displayAddress.substring(0, displayAddress.length() - 2);

        return displayAddress;
    }

    public String toString() {
        return getAddress();
    }

}
