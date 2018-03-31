package com.sogukj.pe.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.adapter.WheelAdapter;
import com.bigkoo.pickerview.lib.WheelView;
import com.bigkoo.pickerview.listener.OnItemSelectedListener;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.Calendar;
import com.ldf.calendar.view.MonthPager;
import com.sogukj.pe.R;
import com.sogukj.pe.ui.calendar.CustomDayView;
import com.sogukj.pe.util.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by sogubaby on 2018/3/30.
 */

public class CalendarDingDing extends View {

    private ViewGroup decorView;
    private ViewGroup rootView;
    private MonthPager mCalendarDateView;
    private CalendarViewAdapter calendarAdapter;
    private TabLayout tabs;
    private WheelView mHour;
    private WheelView mMinute;
    private LinearLayout mMain, mSub;
    private int[] data;
    private TextView mOK;

    public CalendarDingDing(Context context) {
        super(context);
        init(context);
    }

    public CalendarDingDing(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarDingDing(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (decorView == null) {
            decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        }
        rootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.calendar_dingding, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mCalendarDateView = (MonthPager) rootView.findViewById(R.id.calendarDateView);
        tabs = (TabLayout) rootView.findViewById(R.id.tabs);
        mHour = (WheelView) rootView.findViewById(R.id.hour);
        mMinute = (WheelView) rootView.findViewById(R.id.minute);
        mMain = (LinearLayout) rootView.findViewById(R.id.main);
        mSub = (LinearLayout) rootView.findViewById(R.id.sub);
        mOK = (TextView) rootView.findViewById(R.id.confirm);
        mOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rootView.getParent() != null) {
                    decorView.removeView(rootView);
                }
            }
        });

        //时分秒
        final ArrayList<Integer> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }
        mHour.setCyclic(true);
        mHour.setAdapter(new MyAdapter(hours));
        mHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                data[3] = hours.get(index);
                tabs.getTabAt(1).setText(getDisPlayNumber(data[3]) + ":" + getDisPlayNumber(data[4]));
            }
        });
        final ArrayList<Integer> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(i);
        }
        mMinute.setCyclic(true);
        mMinute.setAdapter(new MyAdapter(minutes));
        mMinute.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                data[4] = minutes.get(index);
                tabs.getTabAt(1).setText(getDisPlayNumber(data[3]) + ":" + getDisPlayNumber(data[4]));
            }
        });

        //年月日
        mCalendarDateView.setViewheight(Utils.dpToPx(context, 270));
        OnSelectDateListener onSelectDateListener = new OnSelectDateListener() {
            @Override
            public void onSelectDate(CalendarDate date) {
                //your code
            }

            @Override
            public void onSelectOtherMonth(int offset) {
                //偏移量 -1表示上一个月 ， 1表示下一个月
                mCalendarDateView.selectOtherMonth(offset);
            }
        };
        CustomDayView dayView = new CustomDayView(context, R.layout.custom_day);
        calendarAdapter = new CalendarViewAdapter(context,
                onSelectDateListener,
                CalendarAttr.CalendayType.MONTH, dayView);
        calendarAdapter.weekArrayType = 1;
        mCalendarDateView.setCurrentItem(MonthPager.CURRENT_DAY_INDEX);
        mCalendarDateView.setAdapter(calendarAdapter);
        mCalendarDateView.addOnPageChangeListener(new MonthPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
//                mCurrentPage = position;
//                currentCalendars = calendarAdapter.getAllItems();
//                if(currentCalendars.get(position % currentCalendars.size()) instanceof Calendar){
//                    //you code
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //calendarAdapter.notifyDataChanged(new CalendarDate());

        //切换
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    mMain.setVisibility(View.VISIBLE);
                    mSub.setVisibility(View.GONE);
                } else if (position == 1) {
                    mMain.setVisibility(View.GONE);
                    mSub.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }


//    public void setData(Date date, boolean hasHourMinute) {
//        data = CalendarUtil.getYMDHM(date);
//        tabs.getTabAt(0).setText(data[0] + "-" + getDisPlayNumber(data[1]) + "-" + getDisPlayNumber(data[2]));
//        tabs.getTabAt(1).setText(getDisPlayNumber(data[3]) + ":" + getDisPlayNumber(data[4]));
//
//        mHour.setCurrentItem(data[3]);
//        mMinute.setCurrentItem(data[4]);
//    }

    public void show() {
        if (rootView.getParent() == null) {
            decorView.addView(rootView);
        }
    }

    public void set() {
        calendarAdapter.notifyDataChanged(new CalendarDate());
    }

    private String getDisPlayNumber(int num) {
        return num < 10 ? "0" + num : "" + num;
    }

    class MyAdapter implements WheelAdapter<Integer> {
        private ArrayList<Integer> data = new ArrayList<>();

        public MyAdapter(ArrayList<Integer> list) {
            data.addAll(list);
        }

        @Override
        public int getItemsCount() {
            return data.size();
        }

        @Override
        public Integer getItem(int index) {
            return data.get(index);
        }

        @Override
        public int indexOf(Integer o) {
            return o.intValue();
        }
    }
}
