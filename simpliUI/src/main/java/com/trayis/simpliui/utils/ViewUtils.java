package com.trayis.simpliui.utils;

import android.view.View;
import android.view.ViewOutlineProvider;

public class ViewUtils {

    static final ValueAnimatorCompat.Creator DEFAULT_ANIMATOR_CREATOR = () -> new ValueAnimatorCompat(new ValueAnimatorCompatImpl());

    public static void setBoundsViewOutlineProvider(View view) {
        view.setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }

    public static ValueAnimatorCompat createAnimator() {
        return DEFAULT_ANIMATOR_CREATOR.createAnimator();
    }

}
