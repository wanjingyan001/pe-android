package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_feng_kong.*
import org.jetbrains.anko.textColor
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.JinDiaoItem
import com.sogukj.pe.bean.TouHouManageItem
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.UnknownHostException


class FengKongActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity) {
            val intent = Intent(ctx, FengKongActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    var jin = ArrayList<JinDiaoItem>()
    var touhou = ArrayList<TouHouManageItem>()
    lateinit var adapter: RecyclerAdapter<GradeCheckBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feng_kong)

        setBack(true)
        setTitle("考核评分")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

//        val spannableString = SpannableString("评分标准:每个参与尽调项目得20分;最高分数:120分")
//        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#FFE95C4A")), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#FF323232")), 5, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//        std1.text = spannableString
//
//        val spannable1 = SpannableString("评分标准:优秀101~120分/良好81~100分/合格61~80分/不称职0~60分")
//        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FFE95C4A")), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FF323232")), 5, spannable1.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//        std2.text = spannable1

//        var adapter = FengKongAdapter(context)
//        list.adapter = adapter
//
//        adapter.addAll(arrayListOf(Bean(), Bean(), Bean()))
//
//        inflater = LayoutInflater.from(context)
//
//        add_item.setOnClickListener {
//            var item = inflater.inflate(R.layout.fengkong_item, null) as LinearLayout
//            items.addView(item)
//        }

        btn_commit.setOnClickListener {
            SoguApi.getService(application)
                    .risk_add(jin, touhou)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {

                            }
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        when (e) {
                            is JsonSyntaxException -> showToast("后台数据出错")
                            is UnknownHostException -> showToast("网络出错")
                            else -> showToast("未知错误")
                        }
                    })
        }

        SoguApi.getService(application)
                .check(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            fengkong?.let {
                                var adapter = FengKongHeadAdapter(context, it)
                                list.adapter = adapter
                            }
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
    }

    class FengKongHeadAdapter(val context: Context, val list: ArrayList<GradeCheckBean.FengKongItem>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (getItemViewType(position) == 1) {
                view = LayoutInflater.from(context).inflate(R.layout.fengkong_head_1, null)

                var std = view.findViewById(R.id.std1) as TextView
                std.text = initString(list.get(position).biaozhun!!)

                var listView = view.findViewById(R.id.myList) as MyListView
                var inner_adapter = FengKongAdapter(context, 1)
                listView.adapter = inner_adapter
                inner_adapter.add(GradeCheckBean.FengKongItem.FengKongInnerItem())

                var add_item = view.findViewById(R.id.add_item) as TextView
                add_item.setOnClickListener {
                    inner_adapter.add(GradeCheckBean.FengKongItem.FengKongInnerItem())
                }

            } else if (getItemViewType(position) == 0) {
                view = LayoutInflater.from(context).inflate(R.layout.fengkong_head_2, null)

                var std = view.findViewById(R.id.std2) as TextView
                std.text = initString(list.get(position).biaozhun!!)

                var listView = view.findViewById(R.id.mylist) as MyListView
                var inner_adapter = FengKongAdapter(context, 0)
                listView.adapter = inner_adapter

                inner_adapter.addAll(list.get(position).data!!)
            }
            return view!!
        }

        override fun getItemViewType(position: Int): Int {
            return list.get(position).is_btn!!
        }

        //is_btn---0,1
        override fun getViewTypeCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

        fun initString(str: String): SpannableString {
            val spannable1 = SpannableString("评分标准:${str}")
            spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FFE95C4A")), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FF323232")), 5, spannable1.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return spannable1
        }
    }
}
