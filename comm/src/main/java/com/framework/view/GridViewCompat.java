package com.framework.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by Mars on 2016/2/19.
 */
public class GridViewCompat extends GridView {
    public GridViewCompat(Context context) {
        super(context);
    }

    public GridViewCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
