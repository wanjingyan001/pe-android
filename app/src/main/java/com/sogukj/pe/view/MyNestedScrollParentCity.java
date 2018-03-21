package com.sogukj.pe.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.OverScroller;
import android.widget.ScrollView;

/**
 * Created by lmj on 2016/10/27 0027. on 下午 8:38
 * limengjie
 */
public class MyNestedScrollParentCity extends LinearLayout implements NestedScrollingParent {
    private String Tag = "MyNestedScrollParentCity";
    private LinearLayout mLayout;
    private FrameLayout mFrame;
    private ViewGroup currentContentView;
    private NestedScrollingParentHelper mParentHelper;
    private int mLayoutHeight;
    private int mFrameHeight;
    private Context context;
    private int maxVelocity;
    private int minVelocity;
    private VelocityTracker mVelocityTracker;
    private OverScroller mScroller;
    private int yVelocity;
    private int mLastTouchY;

    public MyNestedScrollParentCity(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MyNestedScrollParentCity(Context context) {
        super(context);
        this.context = context;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLayout = (LinearLayout) getChildAt(0);//顶部layout，包括已投
        mFrame = (FrameLayout) getChildAt(1);
        mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mLayoutHeight <= 0) {
                    mLayoutHeight = mLayout.getMeasuredHeight();
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //TwinklingRefreshLayout layout = (TwinklingRefreshLayout) viewPager.getChildAt(0);//fragment_fund_list
        currentContentView = (ViewGroup) mFrame.getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup.LayoutParams params1 = mFrame.getLayoutParams();
        params1.height = getMeasuredHeight() - mLayoutHeight;

        ViewGroup.LayoutParams params = mFrame.getChildAt(0).getLayoutParams();
        params.height = getMeasuredHeight() - mLayoutHeight;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.i(Tag, "onStartNestedScroll--" + "child:" + child + ",target:" + target + ",nestedScrollAxes:" + nestedScrollAxes);
        return true;
    }

    private void init() {
        mParentHelper = new NestedScrollingParentHelper(this);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
        minVelocity = configuration.getScaledMinimumFlingVelocity();
        mScroller = new OverScroller(context);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Log.i(Tag, "onNestedScrollAccepted" + "child:" + child + ",target:" + target + ",nestedScrollAxes:" + nestedScrollAxes);
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.i(Tag, "onStopNestedScroll--target:" + target);
        mParentHelper.onStopNestedScroll(target);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.i(Tag, "onNestedScroll--" + "target:" + target + ",dxConsumed" + dxConsumed + ",dyConsumed:" + dyConsumed
                + ",dxUnconsumed:" + dxUnconsumed + ",dyUnconsumed:" + dyUnconsumed);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        if (showImg(dy) || hideImg(dy)) {//如果父亲自己要滑动，则拦截
            consumed[1] = dy;
            scrollBy(0, dy);
            Log.i("onNestedPreScroll", "Parent滑动：" + dy);
        }
        Log.i(Tag, "onNestedPreScroll--getScrollY():" + getScrollY() + ",dx:" + dx + ",dy:" + dy + ",consumed:" + consumed);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.i(Tag, "onNestedFling--target:" + target);
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.i(Tag, "onNestedPreFling--target:" + target);
        return true;
    }

    @Override
    public int getNestedScrollAxes() {
        Log.i(Tag, "getNestedScrollAxes");
        return 0;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mLayoutHeight) {
            y = mLayoutHeight;
        }

        super.scrollTo(x, y);
    }

    /**
     * 下拉的时候是否要向下滑动显示图片
     */
    public boolean showImg(int dy) {
        if (dy < 0) {
            if (getScrollY() > 0) {//如果parent外框，还可以往上滑动
                if (currentContentView instanceof ScrollView && currentContentView.getScrollY() == 0) {
                    return true;
                } else if (currentContentView instanceof ListView) {

                    if (!currentContentView.canScrollVertically(-1)) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    /**
     * 上拉的时候，是否要向上滑动，隐藏图片
     *
     * @return
     */
    public boolean hideImg(int dy) {
        if (dy > 0) {
            if (getScrollY() < mLayoutHeight) {//如果parent外框，还可以往下滑动
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.i("aaa", "getY():getRawY:" + event.getRawY());
        initVelocity(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchY = (int) (event.getRawY() + 0.5f);
                reset();
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) (event.getRawY() + 0.5f);
                int dy = mLastTouchY - y;
                mLastTouchY = y;
                if (showImg(dy) || hideImg(dy)) {//如果父亲自己要滑动
                    scrollBy(0, dy);
                }
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, maxVelocity);
                yVelocity = (int) mVelocityTracker.getYVelocity();
                Log.i(Tag, "getYVelocity:" + yVelocity + ",minVelocity:" + minVelocity);
                if (Math.abs(yVelocity) > minVelocity) {
                    mScroller.fling(0, getScrollY(), 0, -yVelocity, 0, 0, -50000, 5000);
                    postInvalidate();
                }
                recycleVelocity();
                break;
        }


        return super.dispatchTouchEvent(event);
    }

    public void reset() {
        if (mScroller.computeScrollOffset()) {
            mScroller.abortAnimation();
        }
        yVelocity = 0;
    }

    public void initVelocity(MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

    }

    public void recycleVelocity() {
        if (null != mVelocityTracker) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = null;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            postInvalidate();
            int dy = mScroller.getFinalY() - mScroller.getCurrY();
            if (yVelocity > 0) {//下拉
                Log.e("getScrollY", "" + getScrollY());
                Log.e("isChildScrollToTop", "" + isChildScrollToTop());
                if (getScrollY() >= mLayoutHeight) {//此时top完全隐藏
                    if (isChildScrollToTop()) {//如果子view已经滑动到顶部，这个时候父亲自己滑动
                        scrollBy(0, dy);
                    } else {
                        scrollContentView(dy);
                    }
                } else if (getScrollY() == 0) {//parent自己完全显示，交给子view滑动
                    if (!isChildScrollToTop()) {
                        scrollContentView(dy);
                    }
                } else {//此时top没有完全显示，让parent自己滑动
                    scrollBy(0, dy);
                }
            } else if (yVelocity < 0) {//上拉
                if (getScrollY() >= mLayoutHeight) {//此时top完全隐藏
                    scrollContentView(dy);
                } else {
                    scrollBy(0, dy);
                }

            }
        }
    }

    public void scrollContentView(int dy) {
        if (currentContentView instanceof ScrollView) {
            ((ScrollView) currentContentView).smoothScrollBy(0, dy);
        } else if (currentContentView instanceof ListView) {
            ((ListView) currentContentView).smoothScrollBy(0, dy);
        }
    }

    /**
     * 判断子view是否已经滑动到顶部
     */
    public boolean isChildScrollToTop() {
        if (currentContentView instanceof ScrollView && currentContentView.getScrollY() == 0) {
            return true;
        } else if (currentContentView instanceof ListView) {

            if (!currentContentView.canScrollVertically(-1)) {
                return true;
            }

        }
        return false;
    }

    public void setCurrentContentView(ViewGroup view) {
        currentContentView = view;
    }
}
