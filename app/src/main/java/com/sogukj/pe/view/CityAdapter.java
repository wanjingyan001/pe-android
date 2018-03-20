package com.sogukj.pe.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.bean.CityArea.City;

import java.util.ArrayList;

/**
 * Created by sogubaby on 2018/3/20.
 */

public class CityAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<City> city;

    public CityAdapter(Context context, ArrayList<City> city) {
        this.context = context;
        this.city = city;
    }

    public ArrayList<City> getData() {
        return city;
    }

    @Override
    public int getCount() {
        return city.size();
    }

    @Override
    public City getItem(int position) {
        return city.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.city_item, null);
            holder.tvCity = (TextView) view.findViewById(R.id.city);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvCity.setText(city.get(position).getName());
        if (city.get(position).getSeclected() == true) {
            holder.tvCity.setTextColor(Color.parseColor("#1787fb"));
        } else {
            holder.tvCity.setTextColor(Color.parseColor("#282828"));
        }
        return view;
    }

    final static class ViewHolder {
        TextView tvCity;
    }
}
