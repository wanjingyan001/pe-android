package com.sogukj.pe.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.bean.CityArea;

import java.util.List;

public class ProvinceAdapter extends BaseAdapter implements SectionIndexer {

    private Context mContext;
    private List<CityArea> list = null;

    /**
     * 构造函数
     */
    public ProvinceAdapter(Context mContext, List<CityArea> list) {
        this.mContext = mContext;
        this.list = list;
    }

    /**
     * 当ListView数据发生变化时，调用此方法来更新ListView
     */
    public void updateListView(List<CityArea> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        final CityArea province = list.get(i);
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item, null);
            holder.tvLetter = (TextView) view.findViewById(R.id.catalog);
            holder.tvTitle = (TextView) view.findViewById(R.id.province);

            holder.mLayout = (LinearLayout) view.findViewById(R.id.display);
            holder.mIv = (ImageView) view.findViewById(R.id.direct);
            holder.mCityLv = (MyListView) view.findViewById(R.id.city);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        //根据position获取分类的首字母的char ascii值
        int section = getSectionForPosition(i);

        //如果当前位置等于该分类首字母的Char的位置，则认为是第一次出现
        if (i == getPositionForSection(section)) {
            holder.tvLetter.setVisibility(View.VISIBLE);
            holder.tvLetter.setText(province.getSortLetters());
        } else {
            holder.tvLetter.setVisibility(View.GONE);
        }

        holder.tvTitle.setText(list.get(i).getName());

        CityAdapter adapter = new CityAdapter(mContext, province.getCity());
        holder.mCityLv.setAdapter(adapter);

        final ImageView iv = holder.mIv;
        final MyListView city_lv = holder.mCityLv;
        if (province.getSeclected() == true) {
            iv.setBackgroundResource(R.drawable.up);
            city_lv.setVisibility(View.VISIBLE);
        } else {
            iv.setBackgroundResource(R.drawable.down);
            city_lv.setVisibility(View.GONE);
        }
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (province.getSeclected() == false) {
                    for (int index = 0; index < list.size(); index++) {
                        if (index == i) {
                            list.get(index).setSeclected(true);
                        } else {
                            list.get(index).setSeclected(false);
                        }
                    }
                } else if (province.getSeclected() == true) {
                    province.setSeclected(false);
                }
                notifyDataSetChanged();
            }
        });

        return view;
    }

    /**
     * ViewHolder类
     */
    final static class ViewHolder {
        TextView tvLetter;
        TextView tvTitle;
        LinearLayout mLayout;
        ImageView mIv;
        MyListView mCityLv;
    }

    /**
     * 根据ListView的当前位置获取匪类的首字母的Char ascii值
     *
     * @param position
     * @return
     */
    public int getSectionForPosition(int position) {
        return list.get(position).getSortLetters().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     *
     * @param section
     * @return
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 提取英文的首字母，非英文字母用#代替
     *
     * @param str
     * @return
     */
    private String getAlpha(String str) {
        String sortStr = str.trim().substring(0, 1).toUpperCase();
        //正则表达式，判断首字母是否是英文字母
        if (sortStr.matches("[A-Z]")) {
            return sortStr;
        } else {
            return "#";
        }
    }

    @Override
    public Object[] getSections() {
        return null;
    }
}
