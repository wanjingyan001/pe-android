package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sogukj.pe.R;
import com.sogukj.pe.bean.ScheduleBean;
import com.sogukj.pe.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/12/9.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int HEAD = 1, ITEM = 2, EMPTY = 3;
    private Context context;
    private List<ScheduleBean> data;
    private ScheduleItemClickListener listener;

    public ScheduleAdapter(Context context, List<ScheduleBean> data) {
        this.context = context;
        this.data = data;
    }

    public void setListener(ScheduleItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEAD) {
            return new HeadHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_dayofmonth, parent, false));
        } else if (viewType == ITEM) {
            return new ItemHolder(LayoutInflater.from(context).inflate(R.layout.item_schedule_info, parent, false));
        } else {
            return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.item_empty, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (data.size() > 0) {
            ScheduleBean bean = data.get(position);
            if (bean != null) {
                try {
                    if (holder instanceof HeadHolder) {
                        long startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bean.getStart_time()).getTime();
                        if (Utils.getTime(new Date(startTime), "yyyy年MM月dd日")
                                .equals(Utils.getTime(System.currentTimeMillis(), "yyyy年MM月dd日"))) {
                            ((HeadHolder) holder).dayTv.setText("今天");
                            ((HeadHolder) holder).month.setVisibility(View.GONE);
                        } else {
                            ((HeadHolder) holder).dayTv.setText(Utils.getDayFromDate(startTime));
                            ((HeadHolder) holder).month.setVisibility(View.VISIBLE);
                            ((HeadHolder) holder).month.setText(Utils.getWeek(startTime) + "\n" + Utils.getTime(startTime, "yyyy年MM月"));
                        }
                    } else if (holder instanceof ItemHolder) {
                        Log.d("WJY", position + "===>" + new Gson().toJson(bean));
                        if (bean.getEnd_time() != null) {
                            long startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bean.getStart_time()).getTime();
                            long endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bean.getEnd_time()).getTime();
                            ((ItemHolder) holder).startTime.setText(Utils.getTime(startTime));
                            ((ItemHolder) holder).endTime.setText(Utils.getTime(endTime));
                        }
                        ((ItemHolder) holder).contentTv.setText(bean.getTitle());
                        if (bean.is_finish() == 1) {
                            ((ItemHolder) holder).finishBox.setSelected(true);
                            ((ItemHolder) holder).contentTv.setPaintFlags(
                                    ((ItemHolder) holder).contentTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        } else {
                            ((ItemHolder) holder).finishBox.setSelected(false);
                            ((ItemHolder) holder).contentTv.setPaintFlags(
                                    ((ItemHolder) holder).contentTv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }
                        if (bean.is_collect() != null) {
                            //noinspection ConstantConditions
                            if (bean.is_collect() == 1) {
                                ((ItemHolder) holder).finishBox.setVisibility(View.INVISIBLE);
                                ((ItemHolder) holder).contentTv.getPaint().setTextSkewX(0);
                            } else {
                                ((ItemHolder) holder).finishBox.setVisibility(View.VISIBLE);
                                ((ItemHolder) holder).finishBox.setSelected(true);
                                ((ItemHolder) holder).contentTv.getPaint().setTextSkewX(-0.4f);
                            }
                        }else {
                            ((ItemHolder) holder).contentTv.getPaint().setTextSkewX(0);
                        }
                        ((ItemHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                listener.onItemClick(v, position);
                            }
                        });
                        ((ItemHolder) holder).finishBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("WJY", "isChecked:" + v.isSelected());
                                ((ItemHolder) holder).finishBox.setSelected(!v.isSelected());
                                if (v.isSelected()) {
                                    ((ItemHolder) holder).contentTv.setPaintFlags(
                                            ((ItemHolder) holder).contentTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                } else {
                                    ((ItemHolder) holder).contentTv.setPaintFlags(
                                            ((ItemHolder) holder).contentTv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                }
                                listener.finishCheck(v.isSelected(), position);
                            }
                        });
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else {
            EmptyHolder emptyHolder = (EmptyHolder) holder;
            emptyHolder.emptyView.setImageResource(R.drawable.enpty);
        }
    }

    @Override
    public int getItemCount() {
        return data.size() == 0 ? 1 : data.size();
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

    class HeadHolder extends RecyclerView.ViewHolder {

        private TextView dayTv;
        private TextView month;

        public HeadHolder(View itemView) {
            super(itemView);
            dayTv = ((TextView) itemView.findViewById(R.id.day));
            month = ((TextView) itemView.findViewById(R.id.month));
            itemView.findViewById(R.id.line).setVisibility(View.GONE);
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private TextView startTime;
        private TextView endTime;
        private TextView contentTv;
        private ImageView finishBox;
        private View view;

        public ItemHolder(View itemView) {
            super(itemView);
            view = itemView;
            startTime = ((TextView) itemView.findViewById(R.id.startTime));
            endTime = ((TextView) itemView.findViewById(R.id.endTime));
            contentTv = ((TextView) itemView.findViewById(R.id.contentTv));
            finishBox = ((ImageView) itemView.findViewById(R.id.finishBox));
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
