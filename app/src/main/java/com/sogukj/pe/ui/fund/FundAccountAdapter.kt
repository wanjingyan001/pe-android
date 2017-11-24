package com.sogukj.pe.ui.fund

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sogukj.pe.R


/**
 * Created by admin on 2017/11/24.
 */
class FundAccountAdapter(val ctx: Context?, val dataList: Map<String, String>) : Adapter<FundAccountAdapter.FundAccountHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FundAccountHolder {
        val inflate = LayoutInflater.from(ctx).inflate(R.layout.item_fund_account_list, parent, false)
        return FundAccountHolder(inflate)
    }

    override fun onBindViewHolder(holder: FundAccountHolder?, position: Int) {
    }

    override fun getItemCount(): Int = dataList.size


    inner class FundAccountHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    }
}