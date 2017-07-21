package com.framework.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * 模块：
 * 创建人：mars
 * 负责人：
 * 创建时间：2015/12/2 14:09
 * 备注：
 */
public class ViewUtil {

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float pxToDp(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, context.getResources().getDisplayMetrics());
    }


}
