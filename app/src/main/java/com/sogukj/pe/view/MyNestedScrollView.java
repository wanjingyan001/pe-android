package com.sogukj.pe.view;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.fashare.stack_layout.StackLayout;
import com.sogukj.pe.R;

/**
 * Created by sogubaby on 2018/4/4.
 */

public class MyNestedScrollView extends NestedScrollView {

    public MyNestedScrollView(Context context) {
        super(context);
    }

    public MyNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        layout = (StackLayout) findViewById(R.id.stack_layout);
    }

    private StackLayout layout;

    // 上下滑动   dispatchTouchEvent  onInterceptTouchEvent(2次),  dispatchTouchEvent  onTouchEvent
    // 左右卡片滑动   dispatchTouchEvent   onInterceptTouchEvent
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("MyNestedScrollView", "dispatchTouchEvent");
//        int x = (int) ev.getRawX();
//        int y = (int) ev.getRawY();
//        if (!isTouchPointInView(x, y)) {
//            return true;
//        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e("MyNestedScrollView", "onInterceptTouchEvent");
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        if (isTouchPointInView(x, y)) {
            layout.requestDisallowInterceptTouchEvent(false);
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.e("MyNestedScrollView", "onTouchEvent");
        return super.onTouchEvent(ev);
    }

    //(x,y)是否在layout的区域内
    private boolean isTouchPointInView(int x, int y) {
        if (layout == null) {
            return false;
        }
        int[] location = new int[2];
        layout.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + layout.getMeasuredWidth();
        int bottom = top + layout.getMeasuredHeight();
        if (y >= top && y <= bottom && x >= left
                && x <= right) {
            return true;
        }
        return false;
    }
}
