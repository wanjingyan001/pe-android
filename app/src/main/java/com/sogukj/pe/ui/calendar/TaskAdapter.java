package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/12/7.
 */

public class TaskAdapter extends RecyclerView.Adapter {
    private int day = 1, info = 2;
    private Context context;
    private List<Object> data;

    public TaskAdapter(Context context, List<Object> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == day) {
            return new DayHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_dayofmonth, parent, false));
        } else if (viewType == info) {
            return new InfoHolder(LayoutInflater.from(context).inflate(R.layout.item_task_list, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Object o = data.get(position);
        if (holder instanceof DayHolder) {
            TodoDay day = (TodoDay) o;
            try {
                Date parse = new SimpleDateFormat("yyyy-MM-dd").parse(day.getDayTime());
                if (Utils.getTime(parse, "yyyy年MM月dd日")
                        .equals(Utils.getTime(System.currentTimeMillis(), "yyyy年MM月dd日"))) {
                    ((DayHolder) holder).day.setText("今天");
                    ((DayHolder) holder).month.setVisibility(View.GONE);
                } else {
                    ((DayHolder) holder).day.setText(Utils.getTime(parse, "dd"));
                    ((DayHolder) holder).month.setVisibility(View.VISIBLE);
                    ((DayHolder) holder).month.setText(Utils.getWeek(parse.getTime()) + "\n" + Utils.getTime(parse.getTime(), "yyyy年MM月"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (holder instanceof InfoHolder) {
            final TaskItemBean.ItemBean bean = (TaskItemBean.ItemBean) o;
            ((InfoHolder) holder).time.setText(bean.getEnd_time());
            ((InfoHolder) holder).content.setText(bean.getTitle());
            if (bean.getLeader() == null) {
                ((InfoHolder) holder).typeTv.setVisibility(View.GONE);
            } else {
                ((InfoHolder) holder).typeTv.setVisibility(View.VISIBLE);
                ((InfoHolder) holder).typeTv.setText(bean.getLeader());
            }
            if (bean.is_finish() == 1) {
                ((InfoHolder) holder).finishBox.setSelected(true);
                ((InfoHolder) holder).content.setPaintFlags(((InfoHolder) holder).content.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                ((InfoHolder) holder).finishBox.setSelected(false);
                ((InfoHolder) holder).content.setPaintFlags(((InfoHolder) holder).content.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
            ((InfoHolder) holder).finishBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) {
                        listener.finishCheck(buttonView, isChecked, position);
                    }
                }
            });

            ((InfoHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(v, position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = data.get(position);
        if (o instanceof TodoDay) {
            return day;
        } else if (o instanceof TaskItemBean.ItemBean) {
            return info;
        }
        return info;
    }

    class DayHolder extends RecyclerView.ViewHolder {
        private TextView day;
        private TextView month;

        public DayHolder(View itemView) {
            super(itemView);
            day = ((TextView) itemView.findViewById(R.id.day));
            month = ((TextView) itemView.findViewById(R.id.month));
            itemView.findViewById(R.id.line).setVisibility(View.GONE);
        }
    }

    class InfoHolder extends RecyclerView.ViewHolder {
        private TextView typeTv;
        //        private TextView confirmStatus;
//        private TextView reason;
//        private TextView canAttend;
//        private TextView notAttend;
        private TextView time;
        private CheckBox finishBox;
        private TextView content;
        private View view;

        public InfoHolder(View itemView) {
            super(itemView);
            view = itemView;
            time = ((TextView) itemView.findViewById(R.id.time));
            finishBox = ((CheckBox) itemView.findViewById(R.id.finishBox));
            content = ((TextView) itemView.findViewById(R.id.content));
            typeTv = ((TextView) itemView.findViewById(R.id.typeTv));
//            confirmStatus = ((TextView) itemView.findViewById(R.id.confirmStatus));
//            reason = ((TextView) itemView.findViewById(R.id.reason));
//            canAttend = ((TextView) itemView.findViewById(R.id.canAttend));
//            notAttend = ((TextView) itemView.findViewById(R.id.notAttend));
        }
    }

    private ScheduleItemClickListener listener;

    public void setListener(ScheduleItemClickListener listener) {
        this.listener = listener;
    }
}
