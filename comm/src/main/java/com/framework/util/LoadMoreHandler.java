package com.framework.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;


/**
 * Created by qff on 2015/12/14.
 */
public class LoadMoreHandler implements AbsListView.OnScrollListener, View.OnTouchListener {
    private final ListView mView;
    protected OnLoadMoreListener mListener;
    private View moreView;
    private int mTouchSlop;
    private int y0, y1, y2;
    private boolean isLoading = false;
    boolean isSrollUp = false;
    int b0 = 0;
    static final int MSG_LOADING = 0xF0;
    static final int MSG_LOAD_FINISH = 0xF1;
    final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOADING:
//                    if (mView.getFooterViewsCount() > 0) {
//                        mView.removeFooterView(moreView);
//                        moreView.setVisibility(View.VISIBLE);
//                        mView.postInvalidate();
//                    }
                    mView.addFooterView(moreView);
                    break;

                case MSG_LOAD_FINISH:
                    mView.removeFooterView(moreView);
                    y0 = 0;
                    y1 = 0;
                    break;

            }
        }
    };

    public LoadMoreHandler(ListView view, ListAdapter mAdapter, View moreView) {
        Args.INSTANCE.notNull(view);
        Args.INSTANCE.notNull(moreView);
        this.mView = view;
        this.moreView = moreView;
        this.mView.addFooterView(moreView);
        this.mView.setAdapter(mAdapter);
        this.mView.removeFooterView(moreView);
        this.mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
        this.b0 = mView.getBottom();
        this.mView.setOnScrollListener(this);
        this.mView.setOnTouchListener(this);
    }

    public LoadMoreHandler(ExpandableListView view, BaseExpandableListAdapter mAdapter, View moreView) {
        Args.INSTANCE.notNull(view);
        Args.INSTANCE.notNull(moreView);
        this.mView = view;
        this.moreView = moreView;
        this.mView.addFooterView(moreView);
        view.setAdapter(mAdapter);
        this.mView.removeFooterView(moreView);
        this.mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
        this.b0 = mView.getBottom();
        this.mView.setOnScrollListener(this);
        this.mView.setOnTouchListener(this);
    }

    public LoadMoreHandler(ListView view, ListAdapter mAdapter, View moreView, OnLoadMoreListener loadMoreListener) {
        Args.INSTANCE.notNull(view);
        Args.INSTANCE.notNull(loadMoreListener);
        Args.INSTANCE.notNull(moreView);
        this.mView = view;
        this.mListener = loadMoreListener;
        this.moreView = moreView;
        this.mView.addFooterView(moreView);
        this.mView.setAdapter(mAdapter);
        this.mView.removeFooterView(moreView);
        this.mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
        this.b0 = mView.getBottom();
        this.mView.setOnScrollListener(this);
        this.mView.setOnTouchListener(this);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        checkLoadMore();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isSrollUp = false;
                y0 = (int) event.getRawY();
                y1 = y0;
                y2 = y0;
                break;
            case MotionEvent.ACTION_MOVE:
                y1 = (int) event.getRawY();
                isSrollUp = (y0 - y1) >= mTouchSlop;

                break;
            case MotionEvent.ACTION_UP:
                isSrollUp = false;
                checkLoadMore();
                y0 = 0;
                y1 = 0;
                break;
            default:
                break;
        }
        return false;
    }


    private void checkLoadMore() {
        if (canLoadMore()) {
            if (mListener != null) {
                setLoading(true);
                mListener.onLoadMore();
            }
        }
    }


    private boolean canLoadMore() {
        return !isLoading && (mView.getAdapter() != null && mView.getLastVisiblePosition() == (mView.getAdapter().getCount() - 1)) && isSrollUp;
    }


    /**
     * @param loading
     */
    public void setLoading(boolean loading) {
        isLoading = loading;
        mHandler.sendEmptyMessage(isLoading ? MSG_LOADING : MSG_LOAD_FINISH);
    }


    public static interface OnLoadMoreListener {
        void onLoadMore();
    }
}
