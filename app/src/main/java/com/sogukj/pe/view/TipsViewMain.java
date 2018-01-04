package com.sogukj.pe.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sogukj.pe.R;


/**
 * Created by sogubaby on 2018/1/3.
 */

public class TipsViewMain extends LinearLayout {

    private ImageView icon;
    private TextView mTitle, mSubTitle;
    private LinearLayout root;

    public TipsViewMain(Context context) {
        super(context);
        init(context, null);
    }

    public TipsViewMain(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TipsViewMain(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.tip_view_main, this);

        root = (LinearLayout) findViewById(R.id.root);
        icon = (ImageView) findViewById(R.id.icon);
        mTitle = (TextView) findViewById(R.id.title);
        mSubTitle = (TextView) findViewById(R.id.subtitle);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.title_main);
        int bg_id = ta.getResourceId(R.styleable.title_main_bg, 0);
        root.setBackgroundResource(bg_id);
        int icon_id = ta.getResourceId(R.styleable.title_main_icon_main, 0);
        icon.setImageResource(icon_id);
        String title = ta.getString(R.styleable.title_main_title);
        mTitle.setText(title);
        String subtitle = ta.getString(R.styleable.title_main_sub_title);
        mSubTitle.setText(subtitle);
        ta.recycle();

        if (title.equals("审批")) {
            mTitle.setTextColor(Color.parseColor("#FFFFFFFF"));
            mSubTitle.setTextColor(Color.parseColor("#FFFFB5AF"));
        } else if (title.equals("日历")) {
            mTitle.setTextColor(Color.parseColor("#FFFFFFFF"));
            mSubTitle.setTextColor(Color.parseColor("#FFA3B2FF"));
        } else if (title.equals("联系人")) {
            mTitle.setTextColor(Color.parseColor("#FF0A0E1F"));
            mSubTitle.setTextColor(Color.parseColor("#FFA0A4AA"));
        } else if (title.equals("周报")) {
            mTitle.setTextColor(Color.parseColor("#FF0A0E1F"));
            mSubTitle.setTextColor(Color.parseColor("#FFA0A4AA"));
        }
    }
}
