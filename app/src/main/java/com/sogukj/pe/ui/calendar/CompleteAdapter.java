package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.view.CircleImageView;

import java.util.List;

/**
 * Created by admin on 2017/12/7.
 */

public class CompleteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int year = 1, info = 2;
    private Context context;
    private List<Object> data;

    public CompleteAdapter(Context context, List<Object> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == year) {
            return new YearHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_year, parent, false));
        } else if (viewType == info) {
            return new InfoHolder(LayoutInflater.from(context).inflate(R.layout.item_complete_info, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            Object o = data.get(position);
            if (holder instanceof YearHolder) {
                TodoYear year = (TodoYear) o;
                ((YearHolder) holder).Year.setText(year.getYear());
            } else if (holder instanceof InfoHolder) {
                CompleteInfo info = (CompleteInfo) o;
                ((InfoHolder) holder).completeTime.setText(info.getDate());
                ((InfoHolder) holder).completeInfo.setText(info.getInfo());
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
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
            return year;
        } else if (o instanceof CompleteInfo) {
            return info;
        }else {
            return info;
        }
    }

    class YearHolder extends RecyclerView.ViewHolder {
        private TextView Year;

        public YearHolder(View itemView) {
            super(itemView);
            Year = ((TextView) itemView.findViewById(R.id.yearTv));
        }
    }

    class InfoHolder extends RecyclerView.ViewHolder {
        private TextView completeInfo;
        private TextView completeTime;

        public InfoHolder(View itemView) {
            super(itemView);
            completeInfo = ((TextView) itemView.findViewById(R.id.completeInfo));
            completeTime = ((TextView) itemView.findViewById(R.id.completeTime));

        }
    }
}
