package com.framework.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import com.framework.util.Trace;

import java.lang.reflect.Field;

/**
 * Created by Mars on 2016/2/4.
 */
public class ViewPagerCompat extends ViewPager {

    private PageTransformer pageTransformer;

    public ViewPagerCompat(Context context) {
        super(context);
    }

    public ViewPagerCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public enum Type {
        ZoomOut,
        RotateDown,
        Depth,
    }

    public void setTransformer(Type effect) {
        switch (effect){
            case ZoomOut:
                setPageTransformer(true, new PageTransformerZoomOut());
                break;
            case RotateDown:
                setPageTransformer(true, new PageTransformerRotateDown());
                break;
            case Depth:
                setPageTransformer(true, new PageTransformerDepth());
                break;
        }
    }

    private final Interpolator mInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t + 1.0f;
        }
    };

    public void setDuration(int duration){
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(this.getContext(),
                    mInterpolator);
            field.set(this, scroller);
            scroller.setmDuration(duration);
        } catch (Exception e) {
            Trace.INSTANCE.e("", "", e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
}
