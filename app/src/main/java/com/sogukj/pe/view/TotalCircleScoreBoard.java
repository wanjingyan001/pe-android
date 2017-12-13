package com.sogukj.pe.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.sogukj.pe.R;
import com.sogukj.pe.util.Utils;

import java.text.DecimalFormat;

/**
 * Created by sogubaby on 2017/12/13.
 */

public class TotalCircleScoreBoard extends View {

    public TotalCircleScoreBoard(Context context) {
        super(context);
        init(context);
    }

    public TotalCircleScoreBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TotalCircleScoreBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Paint paint;
    private Bitmap bitmap;
    private Context ctx;

    private void init(Context context) {
        this.paint = new Paint();
        this.paint.setAntiAlias(true); //消除锯齿
        this.paint.setStyle(Paint.Style.STROKE); //绘制空心圆
        ctx = context;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_red);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(300, 300);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(300, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 300);
        }
    }

    //宽高274dp
    //最外层有进度的宽4dp
    //图片宽高252dp

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOut(canvas);
        drawInnerImage(canvas);
        drawTxt(canvas);
    }

    private void drawOut(Canvas canvas) {
        /***********配置画笔*************/
        paint.setColor(Color.parseColor("#ff9b8f"));
        paint.setStyle(Paint.Style.STROKE);//设置画笔类型为STROKE类型（个人感觉是描边的意思）
        paint.setStrokeWidth(Utils.dpToPx(ctx, 4));
        /***********绘制圆弧*************/
        float pad = Utils.dpToPx(ctx, 2);
        RectF rectf_head = new RectF(pad, pad, getWidth() - pad, getHeight() - pad);//确定外切矩形范围
        canvas.drawArc(rectf_head, 120, 3 * step, false, paint);//绘制圆弧，不含圆心
        //一共300，分100次，每次3
    }

    private void drawInnerImage(Canvas canvas) {
        int center = getWidth() / 2;
        Rect rect_src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rect_dst = new Rect(center - Utils.dpToPx(ctx, 252 / 2), center - Utils.dpToPx(ctx, 252 / 2),
                center + Utils.dpToPx(ctx, 252 / 2), center + Utils.dpToPx(ctx, 252 / 2));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawBitmap(bitmap, rect_src, rect_dst, paint);
    }

    private void drawTxt(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setColor(Color.parseColor("#411a1a"));
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 65, ctx.getResources().getDisplayMetrics()));
        String str = df.format(step * txt);
        canvas.drawText(str, Utils.dpToPx(ctx, 60), Utils.dpToPx(ctx, 170), paint);
    }

    private DecimalFormat df = new DecimalFormat("#####0.00");

    private int step = 0;
    private double txt = 0.00;

    public void setDate(int step, double txt) {
        this.step = 100 - step;
        this.txt = txt;
        invalidate();
    }
}
