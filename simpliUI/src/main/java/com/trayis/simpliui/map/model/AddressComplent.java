package com.trayis.simpliui.map.model;

import java.util.Arrays;

import androidx.annotation.NonNull;

/**
 * This file is created by,
 *
 * @author mukundrd
 * @@created on 17/03/19
 */
public class AddressComplent {

    public String long_name;

    public String short_name;

    public String[] types;

    @Override
    public String toString() {
        return "AddressComplent{" +
                "long_name='" + long_name + '\'' +
                ", short_name='" + short_name + '\'' +
                ", types=" + Arrays.toString(types) +
                '}';
    }
}
