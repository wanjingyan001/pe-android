package com.sogukj.pe.view;

/**
 * Created by qff on 2016/3/24.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.sogukj.pe.util.Utils;


public class TipsView extends android.support.v7.widget.AppCompatTextView {
    private int number = 0;
    private int color = Color.RED;

    public TipsView(Context context) {
        super(context);
    }

    public TipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TipsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (number > 0) {
            Paint textPaint = new Paint();
            Rect textRect = new Rect();
            String text = number + "";
            textPaint.setColor(color);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(Utils.dpToPx(getContext(), text.length() == 1 ? 10 : text.length() == 2 ? 9 : 8));
            textPaint.setTypeface(Typeface.SANS_SERIF);
            textPaint.getTextBounds(text, 0, text.length(), textRect);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(text, (float) (getWidth() - textRect.width()), textRect.height(), textPaint);
        }
    }


    public void display(int number, int color) {
        this.number = number;
        this.color = color;
        invalidate();
    }
}