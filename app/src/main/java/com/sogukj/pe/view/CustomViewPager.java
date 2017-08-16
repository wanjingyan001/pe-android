package com.sogukj.pe.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by qinfei on 17/2/24.
 */

public class CustomViewPager extends ViewPager {

    int preX = -1;
    int preY = -1;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public CustomViewPager(Context context) {
        super(context);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            preX = (int) event.getX();
            preY = (int) event.getY();
        } else {
           int dif= (int) (Math.abs((int) event.getX() - preX) - Math.abs(event.getY() - preY));
            if (dif>5) {
                return true;
            } else {
                preX = (int) event.getX();
                preY = (int) event.getY();
            }
        }
        return super.onInterceptTouchEvent(event);
    }
}