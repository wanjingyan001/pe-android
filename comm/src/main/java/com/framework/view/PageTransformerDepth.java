package com.framework.view;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by Mars on 2016/2/4.
 */
public class PageTransformerDepth implements ViewPager.PageTransformer {
    private static final float DEFAULT_SCALE = 0.75f;

    private float scale;

    public PageTransformerDepth() {
        this.scale = DEFAULT_SCALE;
    }

    public PageTransformerDepth(float scale) {
        this.scale = scale;
    }

    @Override
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = scale + (1 - scale) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}
