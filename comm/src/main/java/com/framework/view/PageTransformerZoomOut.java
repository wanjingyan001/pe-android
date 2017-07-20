package com.framework.view;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by Mars on 2016/2/4.
 */
public class PageTransformerZoomOut implements ViewPager.PageTransformer {
    private static final float DEFAULT_SCALE = 0.65f;
    private static final float DEFAULT_ALPHA = 0.2f;

    private float scale;
    private float alpha;

    public PageTransformerZoomOut() {
        this.scale = DEFAULT_SCALE;
        this.alpha = DEFAULT_ALPHA;
    }

    public PageTransformerZoomOut(float scale, float alpha) {
        this.scale = scale;
        this.alpha = alpha;
    }

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 1) //a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
        { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(scale, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            view.setAlpha(alpha + (scaleFactor - scale) / (1 - scale) * (1 - alpha));
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}
