package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.State;
import com.ldf.calendar.interf.IDayRenderer;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.DayView;
import com.sogukj.pe.R;

/**
 * Created by ldf on 17/6/26.
 */

public class CustomDayView extends DayView {

    private final CalendarDate today = new CalendarDate();
    private final View selectedBg;
    private final TextView dateTv;
    private final ImageView marker;
    private CalendarDate currentDay;

    /**
     * 构造器
     *
     * @param context        上下文
     * @param layoutResource 自定义DayView的layout资源
     */
    public CustomDayView(Context context, int layoutResource) {
        super(context, layoutResource);
        selectedBg = findViewById(R.id.selected_bg);
        dateTv = ((TextView) findViewById(R.id.date));
        marker = ((ImageView) findViewById(R.id.maker));
    }

    @Override
    public void refreshContent() {
        renderToday(day.getDate());
        renderSelect(day.getState());
        renderMarker(day.getDate(), day.getState());
        super.refreshContent();
    }

    private void renderMarker(CalendarDate date, State state) {
        if (Utils.loadMarkData().containsKey(date.toString())) {
            if (state == State.SELECT || date.toString().equals(today.toString())) {
                marker.setVisibility(GONE);
            } else {
                marker.setVisibility(VISIBLE);
//                if (Utils.loadMarkData().get(date.toString()).equals("0")) {
//
//                } else {
//
//                }
            }
        } else {
            marker.setVisibility(GONE);
        }
    }

    private void renderSelect(State state) {
        if (state == State.SELECT) {
            selectedBg.setVisibility(VISIBLE);
            dateTv.setTextColor(Color.WHITE);
        } else if (state == State.NEXT_MONTH || state == State.PAST_MONTH) {
            selectedBg.setVisibility(GONE);
            dateTv.setTextColor(getResources().getColor(R.color.text_3));
        } else {
            selectedBg.setVisibility(GONE);
            if (currentDay != null && currentDay.equals(today)) {
                dateTv.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                dateTv.setTextColor(getResources().getColor(R.color.text_1));
            }
        }
    }

    private void renderToday(CalendarDate date) {
        if (date != null) {
            currentDay = date;
            dateTv.setText(date.day + "");
        }
    }

    @Override
    public IDayRenderer copy() {
        return new CustomDayView(context, layoutResource);
    }
}
