package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ldf.calendar.Utils;
import com.ldf.calendar.view.MonthPager;

/**
 * Created by admin on 2017/12/20.
 */

public class RefreshBehavior extends CoordinatorLayout.Behavior<TwinklingRefreshLayout> {
    private int initOffset = -1;
    private int minOffset = -1;
    private Context context;
    private boolean initiated = false;

    public RefreshBehavior(Context context, AttributeSet attrs, Context context1) {
        super(context, attrs);
        this.context = context1;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, TwinklingRefreshLayout child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        MonthPager monthPager = getMonthPager(parent);
        initMinOffsetAndInitOffset(parent, child, monthPager);
        return true;
    }

    private void initMinOffsetAndInitOffset(CoordinatorLayout parent,
                                            TwinklingRefreshLayout child,
                                            MonthPager monthPager) {
        if (monthPager.getBottom() > 0 && initOffset == -1) {
            initOffset = monthPager.getViewHeight();
            saveTop(initOffset);
        }
        if(!initiated) {
            initOffset = monthPager.getViewHeight();
            saveTop(initOffset);
            initiated = true;
        }
        child.offsetTopAndBottom(Utils.loadTop());
        minOffset = getMonthPager(parent).getCellHeight();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, TwinklingRefreshLayout child,
                                       View directTargetChild, View target, int nestedScrollAxes) {
        Log.e("ldf","onStartNestedScroll");
        MonthPager monthPager = (MonthPager) coordinatorLayout.getChildAt(0);
        if (monthPager.getPageScrollState() != ViewPager.SCROLL_STATE_IDLE) {
            return false;
        }
        monthPager.setScrollable(false);
        boolean isVertical = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        int firstRowVerticalPosition =
                (child == null || child.getChildCount() == 0) ? 0 : child.getChildAt(0).getTop();
        boolean recycleviewTopStatus = firstRowVerticalPosition >= 0;
        return isVertical
                && (recycleviewTopStatus || !Utils.isScrollToBottom())
                && child == directTargetChild;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, TwinklingRefreshLayout child,
                                  View target, int dx, int dy, int[] consumed) {
        Log.e("ldf","onNestedPreScroll");
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        if (child.getTop() <= initOffset
                && child.getTop() >= getMonthPager(coordinatorLayout).getCellHeight()) {
            consumed[1] = Utils.scroll(child, dy,
                    getMonthPager(coordinatorLayout).getCellHeight(),
                    getMonthPager(coordinatorLayout).getViewHeight());
            saveTop(child.getTop());
        }
    }

    @Override
    public void onStopNestedScroll(final CoordinatorLayout parent, final TwinklingRefreshLayout child, View target) {
        Log.e("ldf","onStopNestedScroll");
        super.onStopNestedScroll(parent, child, target);
        MonthPager monthPager = (MonthPager) parent.getChildAt(0);
        monthPager.setScrollable(true);
        if (!Utils.isScrollToBottom()) {
            if (initOffset - Utils.loadTop() > Utils.getTouchSlop(context)) {
                com.sogukj.pe.util.Utils.scrollTo(parent, child, getMonthPager(parent).getCellHeight(), 200);
            } else {
                com.sogukj.pe.util.Utils.scrollTo(parent, child, getMonthPager(parent).getViewHeight(), 80);
            }
        } else {
            if (Utils.loadTop() - minOffset > Utils.getTouchSlop(context)) {
                com.sogukj.pe.util.Utils.scrollTo(parent, child, getMonthPager(parent).getViewHeight(), 200);
            } else {
                com.sogukj.pe.util.Utils.scrollTo(parent, child, getMonthPager(parent).getCellHeight(), 80);
            }
        }
    }

    private MonthPager getMonthPager(CoordinatorLayout coordinatorLayout) {
        MonthPager monthPager = (MonthPager) coordinatorLayout.getChildAt(0);
        return monthPager;
    }

    private void saveTop(int top) {
        Utils.saveTop(top);
        if (Utils.loadTop() == initOffset) {
            Utils.setScrollToBottom(false);
        } else if (Utils.loadTop() == minOffset) {
            Utils.setScrollToBottom(true);
        }
    }
}
