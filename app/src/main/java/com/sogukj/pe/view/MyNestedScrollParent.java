package com.sogukj.pe.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sogubaby on 2018/1/12.
 */

public class MyNestedScrollParent extends LinearLayout implements NestedScrollingParent {
    private Toolbar mToolBar;
    private FrameLayout mFrame;
    private MyNestedScrollChild myNestedScrollChild;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private int mToolBarHeight;
    private int mFrameHeight;

    public MyNestedScrollParent(Context context) {
        super(context);
    }

    public MyNestedScrollParent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    }

    //获取子view
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mToolBar = (Toolbar) getChildAt(0);
        mFrame = (FrameLayout) getChildAt(1);
        myNestedScrollChild = (MyNestedScrollChild) getChildAt(2);
        mToolBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mToolBarHeight <= 0) {
                    mToolBarHeight = mToolBar.getMeasuredHeight();
                }
            }
        });
        mFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mFrameHeight <= 0) {
                    mFrameHeight = mFrame.getMeasuredHeight();
                }
            }
        });
    }

    //在此可以判断参数target是哪一个子view以及滚动的方向，然后决定是否要配合其进行嵌套滚动
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.e("parent", "onStartNestedScroll");
        if (child instanceof MyNestedScrollChild) {
            return true;
        }
        return false;
    }


    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Log.e("parent", "onNestedScrollAccepted");
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.e("parent", "onStopNestedScroll");
        mNestedScrollingParentHelper.onStopNestedScroll(target);
    }

    //先于child滚动
    //前3个为输入参数，最后一个是输出参数
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.e("parent", "onNestedPreScroll");
        if (showImg(dy) || hideImg(dy)) {//如果需要显示或隐藏图片，即需要自己(parent)滚动
            scrollBy(0, -dy);//滚动
            consumed[1] = dy;//告诉child我消费了多少
        }
    }

    //后于child滚动
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.e("parent", "onNestedScroll");
    }

    //返回值：是否消费了fling
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.e("parent", "onNestedPreFling");
        return false;
    }

    //返回值：是否消费了fling
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.e("parent", "onNestedFling");
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    //下拉的时候是否要向下滚动以显示图片
    public boolean showImg(int dy) {
        if (dy > 0) {
            if (getScrollY() > 0 && myNestedScrollChild.getScrollY() == 0) {
                return true;
            }
        }

        return false;
    }

    //上拉的时候，是否要向上滚动，隐藏图片
    public boolean hideImg(int dy) {
        if (dy < 0) {
            if (getScrollY() < mToolBarHeight) {
                return true;
            }
        }
        return false;
    }

    //scrollBy内部会调用scrollTo
    //限制滚动范围
    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mToolBarHeight) {
            y = mToolBarHeight;
        }

        super.scrollTo(x, y);
    }
}
