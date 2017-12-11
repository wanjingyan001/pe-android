package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sogukj.pe.R;

import java.util.List;

/**
 * Created by admin on 2017/12/7.
 */

public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int YEAR = 1, DAY = 2, INFO = 3;
    private List<Object> data;
    private Context context;

    public TodoAdapter(List<Object> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == YEAR) {
            return new YearHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_year, parent, false));
        } else if (viewType == DAY) {
            return new DayHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_dayofmonth, parent, false));
        } else if (viewType == INFO) {
            return new InfoHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_info, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object o = data.get(position);
        if (holder instanceof YearHolder) {
            TodoYear year = (TodoYear) o;
            ((YearHolder) holder).Year.setText(year.getYear());
        } else if (holder instanceof DayHolder) {
            TodoDay day = (TodoDay) o;
            ((DayHolder) holder).day.setText(day.getDayTime());
        } else if (holder instanceof InfoHolder) {
            TodoInfo info = (TodoInfo) o;
            ((InfoHolder) holder).time.setText(info.getTime());
            ((InfoHolder) holder).content.setText(info.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = data.get(position);
        if (o instanceof TodoYear) {
            return YEAR;
        } else if (o instanceof TodoDay) {
            return DAY;
        } else if (o instanceof TodoInfo) {
            return INFO;
        }else {
            return INFO;
        }
    }

    class YearHolder extends RecyclerView.ViewHolder {
        private TextView Year;

        public YearHolder(View itemView) {
            super(itemView);
            Year = ((TextView) itemView.findViewById(R.id.yearTv));
        }
    }

    class DayHolder extends RecyclerView.ViewHolder {
        private TextView day;
        private TextView month;

        public DayHolder(View itemView) {
            super(itemView);
            day = ((TextView) itemView.findViewById(R.id.day));
            month = ((TextView) itemView.findViewById(R.id.month));
        }
    }

    class InfoHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private CheckBox finishBox;
        private TextView content;

        public InfoHolder(View itemView) {
            super(itemView);
            time = ((TextView) itemView.findViewById(R.id.time));
            finishBox = ((CheckBox) itemView.findViewById(R.id.finishBox));
            content = ((TextView) itemView.findViewById(R.id.content));
        }
    }
}
