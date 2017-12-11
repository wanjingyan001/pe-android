package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sogukj.pe.R;

import java.util.List;

/**
 * Created by admin on 2017/12/7.
 */

public class TaskFilterWindow extends PopupWindow {
    private Context context;
    private View inflate;
    private RecyclerView filterList;
    private List<String> filters;
    private FilterItemClickListener listener;
    private String tag;


    public TaskFilterWindow(Context context, List<String> filters, FilterItemClickListener listener, String tag) {
        super(context);
        this.context = context;
        this.filters = filters;
        this.listener = listener;
        this.tag = tag;
        init();
    }



    private void init() {
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_task_filter_window, null);
        filterList = (RecyclerView) inflate.findViewById(R.id.taskFilterList);
        filterList.setLayoutManager(new LinearLayoutManager(context));
        filterList.setAdapter(new FilterAdapter());
        this.setContentView(inflate);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        this.setOutsideTouchable(true);
    }


    class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {


        @Override
        public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(context).inflate(R.layout.item_task_filter_list, parent, false);
            return new FilterHolder(inflate);
        }

        @Override
        public void onBindViewHolder(final FilterHolder holder, final int position) {
            holder.filterTv.setText(filters.get(position));
            holder.filterTv.setTag(tag);
            if (listener != null) {
                holder.filterTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        listener.itemClick(v, position, holder.filterTv.getText().toString());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return filters.size();
        }

        class FilterHolder extends RecyclerView.ViewHolder {
            private TextView filterTv;

            public FilterHolder(View itemView) {
                super(itemView);
                filterTv = ((TextView) itemView.findViewById(R.id.filterTv));
            }
        }
    }

    interface FilterItemClickListener {
        void itemClick(View view, int position, String filter);
    }
}
