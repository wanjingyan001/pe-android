package com.framework.view;

import android.support.v4.view.ViewPager;
import android.view.View;


/**
 * Created by Mars on 2016/2/4.
 */
public class PageTransformerRotateDown implements ViewPager.PageTransformer {
    private static final float DEFAULT_ROTATION = 20.0f;

    private float mRotation;
    private float rotation;

    public PageTransformerRotateDown() {
        this.rotation = DEFAULT_ROTATION;
    }

    public PageTransformerRotateDown(float rotation) {
        this.rotation = rotation;
    }

    public void transformPage(View view, float position) {
        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setRotation(0);

        } else if (position <= 1) // a页滑动至b页 ； a页从 0.0 ~ -1 ；b页从1 ~ 0.0
        { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            if (position < 0) {
                mRotation = (rotation * position);
                view.setPivotX(view.getMeasuredWidth() * 0.5f);
                view.setPivotY(view.getMeasuredHeight());
                view.setRotation(mRotation);
            } else {
                mRotation = (rotation * position);
                view.setPivotX(view.getMeasuredWidth() * 0.5f);
                view.setPivotY(view.getMeasuredHeight());
                view.setRotation(mRotation);
            }
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setRotation(0);
        }
    }
}
