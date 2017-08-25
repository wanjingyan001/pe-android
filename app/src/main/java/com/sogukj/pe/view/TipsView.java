package com.sogukj.pe.view;

/**
 * Created by qff on 2016/3/24.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.sogukj.pe.util.Utils;


public class TipsView extends android.support.v7.widget.AppCompatTextView {
    private int number = 0;

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
            String text = number > 9 ? "9+" : number + "";
            textPaint.setColor(Color.WHITE);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(Utils.dpToPx(getContext(), text.length() == 1 ? 10 : text.length() == 2 ? 9 : 8));
            textPaint.setTypeface(Typeface.SANS_SERIF);
            textPaint.getTextBounds(text, 0, text.length(), textRect);
            textPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            int width = Utils.dpToPx(getContext(), 15);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            RectF rect = new RectF(getWidth() / 2, getHeight() / 4, getWidth() / 2 + width, getHeight() / 4 + width);
            canvas.drawOval(rect, paint);
            canvas.drawText(text, rect.right - rect.width() / 2f, rect.bottom - rect.height() / 2f - fontMetrics.descent + (fontMetrics.descent - fontMetrics.ascent) / 2, textPaint);
        }

    }


    public void display(int number) {
        this.number = number;
        invalidate();
    }
}