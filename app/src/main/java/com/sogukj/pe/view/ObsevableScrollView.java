package com.sogukj.pe.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.sogukj.pe.util.ICallback;

public class ObsevableScrollView extends ScrollView {

    private static final String TAG = ObsevableScrollView.class.getSimpleName();

    boolean shouldIntercept = true;
    int scrollY;

    public ObsevableScrollView(Context context) {
        super(context);
    }

    public ObsevableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObsevableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        return 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return shouldIntercept && super.onInterceptTouchEvent(ev);
    }

    public void setTouchMode(boolean shouldIntercept) {
//        Log.e(TAG, "setTouchMode = " + shouldIntercept);
        this.shouldIntercept = shouldIntercept;
    }

    public static class ScrollStatus {
        int l, t, oldl, oldt;

        public ScrollStatus() {
        }

        public ScrollStatus(int l, int t, int oldl, int oldt) {
            this.l = l;
            this.t = t;
            this.oldl = oldl;
            this.oldt = oldt;
        }
    }

    ICallback<ScrollStatus> scrollStatusCallback;

    public void setScrollStatusCallback(ICallback<ScrollStatus> scrollStatusCallback) {
        this.scrollStatusCallback = scrollStatusCallback;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollStatusCallback != null) {
            ScrollStatus scrollStatus = new ScrollStatus(l, t, oldl, oldt);
            scrollStatusCallback.callback(scrollStatus);
        }
    }
}
