package com.taryis.simpliui;

import android.view.View;
import android.view.ViewOutlineProvider;

class ViewUtilsLollipop {

    static void setBoundsViewOutlineProvider(View view) {
        view.setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }

}

