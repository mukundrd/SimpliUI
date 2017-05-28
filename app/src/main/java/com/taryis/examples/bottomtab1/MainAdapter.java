package com.taryis.examples.bottomtab1;

import android.support.v4.view.PagerAdapter;
import android.view.View;

/**
 * Created by Mukund on 28-05-2017.
 */

class MainAdapter extends PagerAdapter {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}
