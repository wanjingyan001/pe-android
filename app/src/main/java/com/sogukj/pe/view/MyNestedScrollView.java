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
    private int x, y;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        super.onInterceptTouchEvent(event);
        Log.e("MyNestedScrollView", "onInterceptTouchEvent");
        boolean intercepted = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x = (int) event.getX();
                y = (int) event.getY();
                intercepted = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!isTouchPointInView(x, y)) {//父容器需要拦截当前点击事件的条件
                    intercepted = true;//调用onTouchEvent滑动
                } else {//卡片
                    intercepted = false;
                }
                Log.e("MyNestedScrollView", "" + intercepted);
                break;
            }
            case MotionEvent.ACTION_UP: {
                intercepted = false;
                break;
            }
            default:
                break;
        }
        return intercepted;
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
        if (y >= top && y <= bottom && x >= left && x <= right) {
            return true;
        }
        return false;
    }
}
