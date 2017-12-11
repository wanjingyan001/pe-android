package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.bean.ScheduleBean;
import com.sogukj.pe.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/12/7.
 */

public class TeamAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int HEAD = 1, ITEM = 2, EMPTY = 3;
    private Context context;
    private List<ScheduleBean> data;
    private ScheduleItemClickListener listener;

    public TeamAdapter(Context context, List<ScheduleBean> data) {
        this.context = context;
        this.data = data;
    }

    public void setListener(ScheduleItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEAD) {
            return new HeadHolder(LayoutInflater.from(context).inflate(R.layout.layout_team_headview, parent, false));
        } else if (viewType == ITEM) {
            return new ItemHolder(LayoutInflater.from(context).inflate(R.layout.item_team_schedule_list, parent, false));
        } else {
            return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.item_empty, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (data.size() > 0) {
            ScheduleBean bean = data.get(position);
            if (bean != null)
                try {
                    long startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bean.getStart_time()).getTime();
                    if (holder instanceof HeadHolder) {
                        if (Utils.getTime(new Date(startTime), "yyyy年MM月dd日")
                                .equals(Utils.getTime(System.currentTimeMillis(), "yyyy年MM月dd日"))) {
                            ((HeadHolder) holder).dayTv.setText("今天");
                            ((HeadHolder) holder).month.setVisibility(View.GONE);
                        } else {
                            ((HeadHolder) holder).dayTv.setText(Utils.getDayFromDate(startTime));
                            ((HeadHolder) holder).month.setVisibility(View.VISIBLE);
                            ((HeadHolder) holder).month.setText(Utils.getWeek(startTime) + "\n" + Utils.getTime(startTime, "yyyy年MM月"));
                        }
                        ((HeadHolder) holder).selectTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                listener.onItemClick(v, position);
                            }
                        });
                    } else if (holder instanceof ItemHolder) {
                        ((ItemHolder) holder).scheduleTime.setText(Utils.getTime(startTime));
                        ((ItemHolder) holder).scheduleContent.setText(bean.getTitle());
                        ((ItemHolder) holder).creator.setText(bean.getName());
                        ((ItemHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                listener.onItemClick(v, position);
                            }
                        });
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
        } else {
            EmptyHolder emptyHolder = (EmptyHolder) holder;
            emptyHolder.emptyView.setImageResource(R.drawable.enpty);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (data.size() == 0) {
            return EMPTY;
        } else {
            if (position == 0) {
                return HEAD;
            } else {
                return ITEM;
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class HeadHolder extends RecyclerView.ViewHolder {
        private TextView selectTv;
        private TextView dayTv;
        private TextView month;

        public HeadHolder(View itemView) {
            super(itemView);
            dayTv = ((TextView) itemView.findViewById(R.id.dayTv));
            month = ((TextView) itemView.findViewById(R.id.month));
            selectTv = ((TextView) itemView.findViewById(R.id.selectTv));
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private TextView scheduleTime;
        private TextView scheduleContent;
        private TextView creator;
        private View view;

        public ItemHolder(View itemView) {
            super(itemView);
            view = itemView;
            scheduleTime = ((TextView) itemView.findViewById(R.id.scheduleTime));
            scheduleContent = ((TextView) itemView.findViewById(R.id.scheduleContent));
            creator = ((TextView) itemView.findViewById(R.id.creator));
        }
    }

    class EmptyHolder extends RecyclerView.ViewHolder {
        private ImageView emptyView;

        public EmptyHolder(View itemView) {
            super(itemView);
            emptyView = ((ImageView) itemView.findViewById(R.id.iv_empty));
        }
    }

}
