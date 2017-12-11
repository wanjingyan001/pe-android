package com.sogukj.pe.ui.calendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sogukj.pe.R;
import com.sogukj.pe.bean.UserBean;
import com.sogukj.pe.view.CircleImageView;

import java.util.List;

/**
 * Created by admin on 2017/12/8.
 */

public class CcPersonAdapter extends RecyclerView.Adapter<CcPersonAdapter.CCHolder> {
    private Context context;
    private List<UserBean> ccPersons;

    public CcPersonAdapter(Context context, List<UserBean> ccPersons) {
        this.context = context;
        this.ccPersons = ccPersons;
    }

    @Override
    public CcPersonAdapter.CCHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CCHolder(LayoutInflater.from(context).inflate(R.layout.item_copy_list, parent, false));
    }

    @Override
    public void onBindViewHolder(CcPersonAdapter.CCHolder holder, final int position) {
        if (position == ccPersons.size()) {
            holder.userHeader.setImageResource(R.drawable.add_cc_pserson);
            holder.userName.setText("添加");
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.addPerson(CcPersonAdapter.class.getSimpleName());
                    }
                }
            });

        } else {
            final UserBean userBean = ccPersons.get(position);
            holder.userName.setText(userBean.getName());
            Glide.with(context)
                    .load(userBean.getUrl())
                    .into(holder.userHeader);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ccPersons.remove(userBean);
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ccPersons.size() + 1;
    }

    public void addData(UserBean userBean) {
        if (!ccPersons.contains(userBean)){
            ccPersons.add(userBean);
            notifyDataSetChanged();
        }
    }



    private AddPersonListener listener;

    public void setListener(AddPersonListener listener) {
        this.listener = listener;
    }

    class CCHolder extends RecyclerView.ViewHolder {
        private CircleImageView userHeader;
        private TextView userName;
        private View view;

        public CCHolder(View itemView) {
            super(itemView);
            view = itemView;
            userHeader = ((CircleImageView) itemView.findViewById(R.id.userHeader));
            userName = ((TextView) itemView.findViewById(R.id.userName));
        }
    }
}