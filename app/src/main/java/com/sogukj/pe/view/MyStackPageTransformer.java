package com.sogukj.pe.view;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.fashare.stack_layout.StackLayout;

/**
 * Created by sogubaby on 2018/1/5.
 */

public class MyStackPageTransformer extends StackLayout.PageTransformer {

    private float mMinScale;// 栈底: 最小页面缩放比
    private float mMaxScale;// 栈顶: 最大页面缩放比
    private int mStackCount;// 栈内页面数
    private float mPowBase;// 基底: 相邻两 page 的大小比例

    public MyStackPageTransformer(float minScale, float maxScale, int stackCount) {
        this.mMinScale = minScale;
        this.mMaxScale = maxScale;
        this.mStackCount = stackCount;
        if (this.mMaxScale < this.mMinScale) {
            throw new IllegalArgumentException("The Argument: maxScale must bigger than minScale !");
        } else {
            this.mPowBase = (float) Math.pow((double) (this.mMinScale / this.mMaxScale), (double) (1.0F / (float) this.mStackCount));
        }
    }

    public MyStackPageTransformer() {
        this(0.6F, 0.9F, 3);
    }

    public final void transformPage(View view, float position, boolean isSwipeLeft) {
        View parent = (View) view.getParent();
        int pageWidth = parent.getMeasuredWidth();
        int pageHeight = parent.getMeasuredHeight();
        view.setPivotX((float) (pageWidth / 2));
        view.setPivotY((float) pageHeight);
        float bottomPos = (float) (this.mStackCount - 1);
        if (view.isClickable()) {
            view.setClickable(false);
        }

        if (position == -1.0F) {// [-1]: 完全移出屏幕, 待删除
            view.setVisibility(View.GONE);
        } else if (position < 0.0F) {// (-1,0): 拖动中
            view.setVisibility(View.VISIBLE);
            view.setTranslationX(0.0F);
            view.setScaleX(this.mMaxScale);
            view.setScaleY(this.mMaxScale);
        } else if (position <= bottomPos) {// [0, mStackCount-1]: 堆栈中
            int index = (int) position;
            float minScale = this.mMaxScale * (float) Math.pow((double) this.mPowBase, (double) (index + 1));
            float maxScale = this.mMaxScale * (float) Math.pow((double) this.mPowBase, (double) index);
            //float curScale = this.mMaxScale * (float) Math.pow((double) this.mPowBase, (double) position);
            view.setVisibility(View.VISIBLE);
            // 从上至下, 调整堆叠位置
            Log.e("" + position, "" + (float) (-pageHeight) * (1.0F - this.mMaxScale) * (bottomPos - position) / bottomPos);
            view.setTranslationY((float) (-pageHeight) * (1.0F - this.mMaxScale) * (bottomPos - position) / bottomPos);
            // 从上至下, 调整卡片大小
            float scaleFactor = minScale + (maxScale - minScale) * (1.0F - Math.abs(position - (float) index));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
            // 只有最上面一张可点击
            if (position == 0.0F && !view.isClickable()) {
                view.setClickable(true);
            }
            //背景色
            LinearLayout content = (LinearLayout) ((LinearLayout) view).getChildAt(0);
            GradientDrawable myGrad = (GradientDrawable) content.getBackground();
            //myGrad.setColor((mStackCount - (int) position) * (Color.WHITE - Color.BLACK) / mStackCount + Color.BLACK);
            myGrad.setAlpha(255 / mStackCount * (mStackCount - (int) position));
        } else {// (mStackCount-1, +Infinity]: 待显示(堆栈中展示不下)
            view.setVisibility(View.GONE);
        }

    }
}