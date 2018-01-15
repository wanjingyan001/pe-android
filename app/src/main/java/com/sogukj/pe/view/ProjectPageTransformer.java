package com.sogukj.pe.view;

import android.annotation.SuppressLint;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.sogukj.pe.R;

/**
 * Created by sogubaby on 2018/1/8.
 */

public class ProjectPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;
    private float MINALPHA = 1.0f;
    private int jiange = 5;

    //这是tab的方向，item方向相反
    private boolean isLeft = false;//左右滑动

    public void setDirection(boolean isLeft) {
        this.isLeft = isLeft;
    }

    @SuppressLint("NewApi")
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        int itemCount = recyclerView.getChildCount();

        Log.e("isLeft", "" + isLeft);
//        Log.e("TAG", view + " , " + position + "");

        if (isLeft) {
            if (position < 0) {//-1到0
                //半透明->不透明
                for (int i = 0; i < itemCount; i++) {
                    //tab往左，item往右
                    //随着i增加，setTranslationX绝对值变大，一直为负
                    recyclerView.getChildAt(i).setTranslationX(position * pageWidth * i / itemCount);
                    try {
                        Thread.sleep(jiange);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {//  0到1
                //不透明->半透明
                for (int i = 0; i < itemCount; i++) {
                    //tab往左，item往右---从屏幕外进来
                    //随着i增加，setTranslationX绝对值变大，一直为负
                    recyclerView.getChildAt(i).setTranslationX(-position * pageWidth * i / itemCount);
                    try {
                        Thread.sleep(jiange);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            //不透明->半透明
            if (position < 0) {//0到-1
                for (int i = 0; i < itemCount; i++) {
                    //tab往右，item往左
                    //随着i增加，setTranslationX绝对值变大，一直为正
                    recyclerView.getChildAt(i).setTranslationX(-position * pageWidth * i / itemCount);
                    try {
                        Thread.sleep(jiange);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {//  1到0
                //半透明->不透明
                for (int i = 0; i < itemCount; i++) {
                    //tab往右，item往左---从屏幕外进来
                    //随着i增加，setTranslationX绝对值变大，一直为正
                    recyclerView.getChildAt(i).setTranslationX(position * pageWidth * i / itemCount);
                    try {
                        Thread.sleep(jiange);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
