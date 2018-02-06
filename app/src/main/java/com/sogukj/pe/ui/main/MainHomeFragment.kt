package com.sogukj.pe.ui.main

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.ui.approve.EntryApproveActivity
import com.sogukj.pe.ui.calendar.CalendarMainActivity
import com.sogukj.pe.ui.weekly.WeeklyActivity
import kotlinx.android.synthetic.main.fragment_home.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.fashare.stack_layout.StackLayout
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.ui.approve.SealApproveActivity
import com.sogukj.pe.ui.approve.SignApproveActivity
import com.sogukj.pe.ui.msg.MessageListActivity
import com.sogukj.pe.ui.news.NewsListActivity
import com.sogukj.pe.util.CacheUtils
import com.sogukj.pe.util.ColorUtil
import com.sogukj.pe.view.MyStackPageTransformer
import com.sogukj.util.Store


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
        tv_msg.setOnClickListener {
            NewsListActivity.start(baseActivity)
        }


        adapter = HomeAdapter()
        stack_layout.adapter = adapter

        stack_layout.addPageTransformer(
                MyStackPageTransformer()
        )

        stack_layout.setOnSwipeListener(object : StackLayout.OnSwipeListener() {
            override fun onSwiped(swipedView: View, swipedItemPos: Int, isSwipeLeft: Boolean, itemLeft: Int) {
                //Log.e("tagtagtag", (if (isSwipeLeft) "往左" else "往右") + "移除" + mData.get(swipedItemPos) + "." + "剩余" + itemLeft + "项")
            }
        })

        cache = CacheUtils(context)
        Glide.with(context).asGif().load(R.drawable.loading).into(pb)
        pb.visibility = View.VISIBLE
        doRequest()
    }

    lateinit var adapter: HomeAdapter
    lateinit var cache: CacheUtils

    override fun onDestroy() {
        super.onDestroy()
        cache.close()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            onResume()
        }
    }

    lateinit var totalData: ArrayList<MessageBean>

    override fun onResume() {
        super.onResume()
        doRequest()
    }

    fun doRequest() {
        totalData = ArrayList<MessageBean>()
        var cacheData = cache.getDiskCache("${Store.store.getUser(context)?.uid}")
        if (cacheData != null) {
            adapter.dataList.clear()
            adapter.dataList.addAll(cacheData)
            adapter.notifyDataSetChanged()

            totalData.clear()
            totalData.addAll(cacheData)
        }
        SoguApi.getService(baseActivity!!.application)
                .msgList(status = 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapter.dataList.clear()
                            adapter.dataList.addAll(this)
                            adapter.notifyDataSetChanged()

                            cache.addToDiskCache("${Store.store.getUser(context)?.uid}", this)
                            totalData.clear()
                            totalData.addAll(this)
                        }
                    } else
                        showToast(payload.message)
                    pb.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    //showToast("暂无新数据")
                    ToastError(e)
                    pb.visibility = View.GONE
                    if (adapter.dataList.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                        stack_layout.visibility = View.GONE
                    }
                }, {
                    pb.visibility = View.GONE
                    if (adapter.dataList.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                        stack_layout.visibility = View.GONE
                    }
                })
        SoguApi.getService(baseActivity!!.application)
                .getNumber()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            if (sp == null || sp == 0) {
                                tv_sp.isRedEnable(false)
                            } else {
                                tv_sp.isRedEnable(true)
                            }
                            if (rl == null || rl == 0) {
                                tv_rl.isRedEnable(false)
                            } else {
                                tv_rl.isRedEnable(true)
                            }
                            if (zb == null || zb == 0) {
                                tv_weekly.isRedEnable(false)
                            } else {
                                tv_weekly.isRedEnable(true)
                            }
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    //showToast("暂无新数据")
                    ToastError(e)
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
            try {
                holder.tvTitle?.text = strType
                holder.tvSeq?.text = data.title
            } catch (e: Exception) {
            }
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

            holder.tvMore?.setOnClickListener {
                val intent = Intent(context, MessageListActivity::class.java)
                startActivity(intent)
            }
            holder.ll_content?.setOnClickListener {
                val is_mine = if (data.status == -1 || data.status == 4) 1 else 2
                if (data.type == 2) {
                    //SealApproveActivity.start(context, data, is_mine)
                    val intent = Intent(context, SealApproveActivity::class.java)
                    intent.putExtra("is_mine", is_mine)
                    intent.putExtra(Extras.DATA, data)
                    startActivity(intent)
                } else if (data.type == 3) {
                    //SignApproveActivity.start(context, data, is_mine)
                    val intent = Intent(context, SignApproveActivity::class.java)
                    intent.putExtra(Extras.DATA, data)
                    intent.putExtra("is_mine", is_mine)
                    startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class MyViewHolder(view: View) : StackLayout.ViewHolder(view) {

            var tvTitle: TextView? = null
            var tvSeq: TextView? = null
            var tvNum: TextView? = null
            var tvState: TextView? = null
            var tvFrom: TextView? = null
            var tvType: TextView? = null
            var tvMsg: TextView? = null
            var tvUrgent: TextView? = null
            var ll_content: LinearLayout? = null
            var tvMore: TextView? = null

            init {
                tvTitle = view.findViewById(R.id.tv_title) as TextView
                tvSeq = view.findViewById(R.id.sequense) as TextView
                tvNum = view.findViewById(R.id.tv_num) as TextView
                tvState = view.findViewById(R.id.tv_state) as TextView
                tvFrom = view.findViewById(R.id.tv_from) as TextView
                tvType = view.findViewById(R.id.tv_type) as TextView
                tvMsg = view.findViewById(R.id.tv_msg) as TextView
                tvUrgent = view.findViewById(R.id.tv_urgent) as TextView
                ll_content = view.findViewById(R.id.ll_content) as LinearLayout
                tvMore = view.findViewById(R.id.more) as TextView
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