package com.taryis.simpliui.utils;

import android.os.Build;
import android.view.View;

public class ViewUtils {

    static final ValueAnimatorCompat.Creator DEFAULT_ANIMATOR_CREATOR
            = new ValueAnimatorCompat.Creator() {
        @Override
        public ValueAnimatorCompat createAnimator() {
            return new ValueAnimatorCompat(new ValueAnimatorCompatImplHoneycombMr1());
        }
    };

    private interface ViewUtilsImpl {
        void setBoundsViewOutlineProvider(View view);
    }

    private static class ViewUtilsImplBase implements ViewUtilsImpl {
        @Override
        public void setBoundsViewOutlineProvider(View view) {
            // no-op
        }
    }

    private static class ViewUtilsImplLollipop implements ViewUtilsImpl {
        @Override
        public void setBoundsViewOutlineProvider(View view) {
            ViewUtilsLollipop.setBoundsViewOutlineProvider(view);
        }
    }

    private static final ViewUtilsImpl IMPL;

    static {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 21) {
            IMPL = new ViewUtilsImplLollipop();
        } else {
            IMPL = new ViewUtilsImplBase();
        }
    }

    public static void setBoundsViewOutlineProvider(View view) {
        IMPL.setBoundsViewOutlineProvider(view);
    }

    public static ValueAnimatorCompat createAnimator() {
        return DEFAULT_ANIMATOR_CREATOR.createAnimator();
    }

}