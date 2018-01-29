package com.sogukj.pe.ui.IM;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sogukj.pe.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2018/1/16.
 */

public class TeamMenuWindow extends PopupWindow {
    private RecyclerView conditionsList;
    private List<String> titles = new ArrayList<>();
    private int[] icons = {R.drawable.wd, R.drawable.ysb, R.drawable.sp, R.drawable.yy, R.drawable.pic2, R.drawable.qt};
    private Context context;
    private View inflate;

    public TeamMenuWindow(Context context) {
        super(context);
        init(context);
        setPopWindow();
    }

    public TeamMenuWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setPopWindow();
    }

    private void init(Context context) {
        this.context = context;
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_team_menu_winodow, null);
        conditionsList = (RecyclerView) inflate.findViewById(R.id.filter_conditions);
        conditionsList.setLayoutManager(new GridLayoutManager(context, 3));
        titles.add("文档");
        titles.add("压缩包");
        titles.add("视频");
        titles.add("应用");
        titles.add("图片");
        titles.add("其他");
        FilterAdapter adapter = new FilterAdapter(titles);
        conditionsList.setAdapter(adapter);
    }

    private void setPopWindow() {
        this.setContentView(inflate);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        this.setOutsideTouchable(true);
    }


    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    private void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        backgroundAlpha((Activity) context, 0.5f);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        backgroundAlpha((Activity) context, 1f);
    }

    class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {
        private List<String> titles;

        public FilterAdapter(List<String> titles) {
            this.titles = titles;
        }

        @Override
        public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FilterHolder(LayoutInflater.from(context).inflate(R.layout.item_team_filter_menu, parent, false));
        }

        @Override
        public void onBindViewHolder(FilterHolder holder, int position) {
            holder.filterTv.setText(titles.get(position));
            holder.filterIcon.setImageResource(icons[position]);
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }

        class FilterHolder extends RecyclerView.ViewHolder {
            private ImageView filterIcon;
            private TextView filterTv;

            public FilterHolder(View itemView) {
                super(itemView);
                filterTv = (TextView) itemView.findViewById(R.id.filter_text);
                filterIcon = (ImageView) itemView.findViewById(R.id.filter_icon);
            }
        }
    }

}
