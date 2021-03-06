package com.sogukj.pe.view;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.ScrollView;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.sogukj.pe.R;
import com.sogukj.pe.util.Utils;

/**
 * Created by lmj on 2016/10/27 0027. on 下午 8:38
 * limengjie
 */
public class MyNestedScrollParentFund extends LinearLayout implements NestedScrollingParent {
    private String Tag = "MyNestedScrollParent";
    private LinearLayout mToolBar;
    private FrameLayout mFrame;
    private TabLayout mTabs;
    private ViewGroup currentContentView;
    private ViewPager viewPager;
    private NestedScrollingParentHelper mParentHelper;
    private int mToolBarHeight;
    private int mFrameHeight;
    private int mTouchSlop = 0;
    private Context context;
    private int maxVelocity;
    private int minVelocity;
    private VelocityTracker mVelocityTracker;
    private OverScroller mScroller;
    private int yVelocity;
    private int mLastTouchY;

    public MyNestedScrollParentFund(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MyNestedScrollParentFund(Context context) {
        super(context);
        this.context = context;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mToolBar = (LinearLayout) getChildAt(0);
        mFrame = (FrameLayout) getChildAt(2);
        mTabs = (TabLayout) mFrame.getChildAt(1);
        viewPager = (ViewPager) getChildAt(3);
        mToolBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mToolBarHeight <= 0) {
                    mToolBarHeight = mToolBar.getMeasuredHeight();
                    Log.i(Tag, "mToolBarHeight:" + mToolBarHeight + ",mFrameHeight:" + mFrameHeight);
                }
            }
        });
        mFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mFrameHeight <= 0) {
                    mFrameHeight = mFrame.getMeasuredHeight();
                    Log.i(Tag, "mToolBarHeight:" + mToolBarHeight + ",mFrameHeight:" + mFrameHeight);
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        TwinklingRefreshLayout layout = (TwinklingRefreshLayout) viewPager.getChildAt(0);//fragment_fund_list
        currentContentView = (ViewGroup) layout.getChildAt(0);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.i(Tag, "onStartNestedScroll--" + "child:" + child + ",target:" + target + ",nestedScrollAxes:" + nestedScrollAxes);
        return true;
    }

    private void init() {
        mParentHelper = new NestedScrollingParentHelper(this);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
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
//        CoordinatorLayout
//        RecyclerView

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
        if (y > mToolBarHeight) {
            y = mToolBarHeight;
        }

        super.scrollTo(x, y);
    }

    /**
     * 下拉的时候是否要向下滑动显示图片
     */
    public boolean showImg(int dy) {
        if (dy < 0) {
            //改变view
            Log.e("下拉", dy + "+++" + getScrollY());//dy<0;getScrollY()>0,变小
            changeView();

            if (getScrollY() > 0) {//如果parent外框，还可以往上滑动
                if (currentContentView instanceof ScrollView && currentContentView.getScrollY() == 0) {
                    return true;
                } else if (currentContentView instanceof RecyclerView) {

                    if (!currentContentView.canScrollVertically(-1)) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private void changeView() {
        if (mToolBarHeight == 0) {
            mTabs.setBackgroundResource(R.drawable.tab_bg_1);
            mTabs.setTabTextColors(Color.parseColor("#a0a4aa"), Color.parseColor("#282828"));
            for (int i = 0; i < mTabs.getTabCount(); i++) {
                if (i == mTabs.getSelectedTabPosition()) {
                    setDrawable(i, "1", true);
                } else {
                    setDrawable(i, "1", false);
                }
            }
        } else {
            if (getScrollY() < mToolBarHeight) {//如果parent外框，还可以往下滑动
                mTabs.setBackgroundResource(R.drawable.tab_bg_1);
                mTabs.setTabTextColors(Color.parseColor("#a0a4aa"), Color.parseColor("#282828"));
                for (int i = 0; i < mTabs.getTabCount(); i++) {
                    if (i == mTabs.getSelectedTabPosition()) {
                        setDrawable(i, "1", true);
                    } else {
                        setDrawable(i, "1", false);
                    }
                }
            } else if (getScrollY() >= mToolBarHeight) {
                mTabs.setBackgroundResource(R.drawable.tab_bg_2);
                mTabs.setTabTextColors(Color.parseColor("#ff7bb4fc"), Color.parseColor("#ffffff"));
                for (int i = 0; i < mTabs.getTabCount(); i++) {
                    if (i == mTabs.getSelectedTabPosition()) {
                        setDrawable(i, "2", true);
                    } else {
                        setDrawable(i, "2", false);
                    }
                }
            }
        }
    }

    /**
     * 上拉的时候，是否要向上滑动，隐藏图片
     *
     * @return
     */
    public boolean hideImg(int dy) {
        if (dy > 0) {
            Log.e("上拉", dy + "+++" + getScrollY());//dy>0;getScrollY()>0,变大
            changeView();

            if (getScrollY() < mToolBarHeight) {//如果parent外框，还可以往下滑动
                return true;
            }
        }
        return false;
    }

    //dy_1_unselect

    /**
     * @param index--------(tabs对应的index，分别对应dy,cb等)
     * @param state---------（1，2）
     * @param isSelect--------是否选中
     */
    private void setDrawable(int index, String state, boolean isSelect) {
        String name = "";
        switch (index) {
            case 0:
                name += "cb_";
                break;
            case 1:
                name += "cx_";
                break;
            case 2:
                name += "tc_";
                break;
        }
        name += state;
        if (isSelect) {
            name += "_select";
        } else {
            name += "_unselect";
        }
        int id = getResources().getIdentifier(name, "drawable", context.getPackageName());
        mTabs.getTabAt(index).setIcon(id);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    private int mLastTouchX;

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
//                if (showImg(dy) || hideImg(dy)) {//如果父亲自己要滑动
//                    scrollBy(0, dy);
//                }
                int x = (int) (event.getRawX() + 0.5f);
                int dx = mLastTouchX - x;
                mLastTouchX = x;

                if (Math.abs(dx) > Math.abs(dy)) {

                } else {
                    if (showImg(dy) || hideImg(dy)) {//如果父亲自己要滑动
                        scrollBy(0, dy);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, maxVelocity);
                yVelocity = (int) mVelocityTracker.getYVelocity();
                Log.i(Tag, "getYVelocity:" + yVelocity + ",minVelocity:" + minVelocity);
                if (Math.abs(yVelocity) > minVelocity) {
//                    mScroller.fling(0,getScrollY(), 0, -yVelocity, 0, 0, 0, Math.max(0, nsc.getMeasuredHeight()+imgHeight), 0, nsc.getMeasuredHeight()/2);
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
            int dx = mScroller.getFinalX() - mScroller.getCurrX();
            if (Math.abs(dx) < Math.abs(dy)) {

            } else {
                if (yVelocity > 0) {//下拉
                    Log.e("getScrollY", "" + getScrollY());
                    Log.e("isChildScrollToTop", "" + isChildScrollToTop());
                    //172 false 滑动内部
                    //172 true 滑动父亲
                    //0 true 不变
                    if (getScrollY() >= mToolBarHeight) {//此时top完全隐藏

                        if (isChildScrollToTop()) {//如果子view已经滑动到顶部，这个时候父亲自己滑动
                            scrollBy(0, dy);
                        } else {
                            scrollContentView(dy);
                        }

                    } else if (getScrollY() == 0) {//parent自己完全显示，交给子view滑动
                        if (!isChildScrollToTop()) {
                            scrollContentView(dy);
                        } else {
                            changeView();
                        }
                    } else {//此时top没有完全显示，让parent自己滑动
                        scrollBy(0, dy);
                    }
                } else if (yVelocity < 0) {//上拉
                    changeView();
                    if (getScrollY() >= mToolBarHeight) {//此时top完全隐藏
                        scrollContentView(dy);
                    } else {

                        scrollBy(0, dy);
                    }

                }
            }
//            if (yVelocity > 0) {//下拉
//                Log.e("getScrollY", "" + getScrollY());
//                Log.e("isChildScrollToTop", "" + isChildScrollToTop());
//                //172 false 滑动内部
//                //172 true 滑动父亲
//                //0 true 不变
//                if (getScrollY() >= mToolBarHeight) {//此时top完全隐藏
//
//                    if (isChildScrollToTop()) {//如果子view已经滑动到顶部，这个时候父亲自己滑动
//                        scrollBy(0, dy);
//                    } else {
//                        scrollContentView(dy);
//                    }
//
//                } else if (getScrollY() == 0) {//parent自己完全显示，交给子view滑动
//                    if (!isChildScrollToTop()) {
//                        scrollContentView(dy);
//                    } else {
//                        changeView();
//                    }
//                } else {//此时top没有完全显示，让parent自己滑动
//                    scrollBy(0, dy);
//                }
//            } else if (yVelocity < 0) {//上拉
//                changeView();
//                if (getScrollY() >= mToolBarHeight) {//此时top完全隐藏
//                    scrollContentView(dy);
//                } else {
//
//                    scrollBy(0, dy);
//                }
//
//            }

        }
    }

    public void scrollContentView(int dy) {
        if (currentContentView instanceof ScrollView) {

            ((ScrollView) currentContentView).smoothScrollBy(0, dy);
        } else if (currentContentView instanceof RecyclerView) {
            ((RecyclerView) currentContentView).smoothScrollBy(0, dy);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = getMeasuredHeight() - mFrameHeight - Utils.dpToPx(context, 50);//50dp是底部导航栏的高度
    }


    /**
     * 判断子view是否已经滑动到顶部
     */
    public boolean isChildScrollToTop() {
        if (currentContentView instanceof ScrollView && currentContentView.getScrollY() == 0) {
            return true;
        } else if (currentContentView instanceof RecyclerView) {

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
