package com.sogukj.pe.ui.main

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.ui.approve.EntryApproveActivity
import com.sogukj.pe.ui.calendar.CalendarMainActivity
import com.sogukj.pe.ui.msg.MessageListActivity
import com.sogukj.pe.ui.weekly.WeeklyActivity
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import kotlinx.android.synthetic.main.fragment_home.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout
import com.fashare.stack_layout.StackLayout
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_msg_center.*
import com.fashare.stack_layout.transformer.AngleTransformer
import com.fashare.stack_layout.transformer.AlphaTransformer
import com.fashare.stack_layout.transformer.StackPageTransformer
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.util.ColorUtil
import com.sogukj.pe.view.MyStackPageTransformer


/**
 * Created by qinfei on 17/10/11.
 */
class MainHomeFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_home

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //消息
//        tv_zx.setOnClickListener {
//            NewsListActivity.start(baseActivity)
//        }

//        tv_me.setOnClickListener {
//            UserFragment.start(baseActivity)
//        }
        tv_sp.setOnClickListener {
            EntryApproveActivity.start(baseActivity)
        }
        tv_weekly.setOnClickListener { WeeklyActivity.start(baseActivity) }
//        tv_jj.setOnClickListener { FundMainFragment.start(baseActivity) }
        tv_rl.setOnClickListener { CalendarMainActivity.start(baseActivity) }
//        disable(tv_jj)
//        disable(tv_rl)
        //disable(tv_lxr)


        adapter = HomeAdapter()
        stack_layout.adapter = adapter

        stack_layout.addPageTransformer(
                MyStackPageTransformer()
        )

        stack_layout.setOnSwipeListener(object : StackLayout.OnSwipeListener() {
            override fun onSwiped(swipedView: View, swipedItemPos: Int, isSwipeLeft: Boolean, itemLeft: Int) {
                //Log.e("tagtagtag", (if (isSwipeLeft) "往左" else "往右") + "移除" + mData.get(swipedItemPos) + "." + "剩余" + itemLeft + "项")

                // 少于5条, 加载更多
                if (itemLeft < 5) {
                    // TODO: loadmore
                }
            }
        })

        doRequest()
    }

    lateinit var adapter: HomeAdapter

    fun doRequest() {
        SoguApi.getService(baseActivity!!.application)
                .msgList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                            adapter.notifyDataSetChanged()
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
    }

    inner class HomeAdapter : StackLayout.Adapter<HomeAdapter.MyViewHolder>() {

        var dataList = ArrayList<MessageBean>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(
                    context).inflate(R.layout.item_msg_content_main, parent, false))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            var data = dataList[position]
            val strType = when (data.type) {
                1 -> "出勤休假"
                2 -> "用印审批"
                3 -> "签字审批"
                else -> ""
            }
//            if (data.status == 1) {
//                holder.ll_content?.setBackgroundResource(R.drawable.bg_pop_msg_left_1)
//            } else {
//                holder.ll_content?.setBackgroundResource(R.drawable.bg_pop_msg_left)
//            }
            ColorUtil.setColorStatus(holder.tvState!!, data)
            holder.tvTitle?.text = data.title
            holder.tvFrom?.text = "发起人:" + data.username
            holder.tvType?.text = "类型:" + data.type_name
            holder.tvMsg?.text = "审批事由:" + data.reasons
            val cnt = data.message_count
            holder.tvNum?.text = "${cnt}"
            if (cnt != null && cnt > 0)
                holder.tvNum?.visibility = View.VISIBLE
            else
                holder.tvNum?.visibility = View.GONE
            val urgnet = data.urgent_count
            holder.tvUrgent?.text = "加急x${urgnet}"
            if (urgnet != null && urgnet > 0)
                holder.tvUrgent?.visibility = View.VISIBLE
            else
                holder.tvUrgent?.visibility = View.GONE
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class MyViewHolder(view: View) : StackLayout.ViewHolder(view) {

            var tvTitle: TextView? = null
            var tvNum: TextView? = null
            var tvState: TextView? = null
            var tvFrom: TextView? = null
            var tvType: TextView? = null
            var tvMsg: TextView? = null
            var tvUrgent: TextView? = null
            var ll_content: LinearLayout? = null

            init {
                tvTitle = view.findViewById(R.id.tv_title) as TextView
                tvNum = view.findViewById(R.id.tv_num) as TextView
                tvState = view.findViewById(R.id.tv_state) as TextView
                tvFrom = view.findViewById(R.id.tv_from) as TextView
                tvType = view.findViewById(R.id.tv_type) as TextView
                tvMsg = view.findViewById(R.id.tv_msg) as TextView
                tvUrgent = view.findViewById(R.id.tv_urgent) as TextView
                ll_content = view.findViewById(R.id.ll_content) as LinearLayout
            }
        }
    }

    val colorGray = Color.parseColor("#D9D9D9")
    fun disable(view: TextView) {
        view.compoundDrawables[1]?.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
        view.setOnClickListener(null)
    }

    companion object {
        val TAG = MainHomeFragment::class.java.simpleName

        fun newInstance(): MainHomeFragment {
            val fragment = MainHomeFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}