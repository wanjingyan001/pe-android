package com.sogukj.pe.ui.IM

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.view.CircleImageView

/**
 * Created by admin on 2018/1/25.
 */
class MemberAdapter(val ctx: Context, private val members: ArrayList<UserBean>) : RecyclerView.Adapter<MemberAdapter.MemberHolder>() {

    var onItemClick: ((v: View, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberHolder {
        val inflate = LayoutInflater.from(ctx).inflate(R.layout.item_team_member, parent, false)
        return MemberHolder(inflate)
    }

    override fun onBindViewHolder(holder: MemberHolder, position: Int) {
        if (position == members.size) {
            holder.headImg.setImageResource(R.drawable.invalid_name3)
        } else {
            val userBean = members[position]
            if (userBean.url.isNullOrEmpty()) {
                val ch = userBean.name.first()
                holder.headImg.setChar(ch)
            } else {
                Glide.with(ctx)
                        .load(userBean.headImage())
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(holder.headImg)
            }
        }
        holder.headImg.setOnClickListener { v ->
            if (null != onItemClick) {
                onItemClick!!(v, position)
            }
            //TeamSelectActivity.startForResult(ctx, true, members, false)
        }
    }

    override fun getItemCount(): Int = members.size + 1

    class MemberHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headImg: CircleImageView = itemView.findViewById(R.id.member_headImg) as CircleImageView

    }
}