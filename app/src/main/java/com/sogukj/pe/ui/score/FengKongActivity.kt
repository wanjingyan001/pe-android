package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.View
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_feng_kong.*
import org.jetbrains.anko.textColor
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.JinDiaoItem
import com.sogukj.pe.bean.TouHouManageItem
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.header.*
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
    lateinit var adapter: FengKongHeadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feng_kong)

        var bean = Store.store.getUser(context)
        bean?.let {
            Glide.with(context).load(it.url).into(icon)
            name.text = it.name
            depart.text = it.depart_name
            position.text = it.position
        }

        setBack(true)
        setTitle("风控部门评分标准填写")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        btn_commit.setOnClickListener {
            var sub_jin = ArrayList<JinDiaoItem>()
            var sub_touhou = ArrayList<TouHouManageItem>()
            for (item in jin) {
                if (item.info.isNullOrEmpty() || item.title.isNullOrEmpty()) {
                    if (sub_jin.size == 0) {
                        return@setOnClickListener
                    }
                } else {
                    sub_jin.add(item)
                }
            }
            for (item in touhou) {
                if (item.info.isNullOrEmpty()) {
                    if (sub_touhou.size == 0) {
                        return@setOnClickListener
                    }
                } else {
                    sub_touhou.add(item)
                }
            }

            var jin__ = ArrayList<HashMap<String, String>>()
            for (item in sub_jin) {
                var jin_item = HashMap<String, String>()
                jin_item.put("target", item.title!!)
                jin_item.put("info", item.info!!)
                jin__.add(jin_item)
            }

            var touhou__ = ArrayList<HashMap<String, String>>()
            for (item in sub_touhou) {
                var touhou_item = HashMap<String, String>()
                touhou_item.put("performance_id", "${item.performance_id}")
                touhou_item.put("info", item.info!!)
                touhou__.add(touhou_item)
            }

            var total = HashMap<String, ArrayList<HashMap<String, String>>>()
            total.put("jdxm", jin__)
            total.put("thgl", touhou__)

            SoguApi.getService(application)
                    .risk_add(total)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            GangWeiListActivity.start(context, Extras.TYPE_EMPLOYEE)
                            //JudgeActivity.start(context, TYPE_EMPLOYEE, FK)
                            finish()
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
                                adapter = FengKongHeadAdapter(context, it, jin, touhou)
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

    class FengKongHeadAdapter(val context: Context, val list: ArrayList<GradeCheckBean.FengKongItem>,
                              val jin: ArrayList<JinDiaoItem>, val touhou: ArrayList<TouHouManageItem>) : BaseAdapter() {

        lateinit var jindiao_adapter: FengKongAdapter
        lateinit var touhou_adapter: FengKongAdapter

        fun getAdapter(): FengKongAdapter {
            return jindiao_adapter
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (getItemViewType(position) == 1) {// jin  diao
                view = LayoutInflater.from(context).inflate(R.layout.fengkong_head_1, null)

                var std = view.findViewById(R.id.std1) as TextView
                std.text = initString(list.get(position).biaozhun!!)

                var listView = view.findViewById(R.id.myList) as MyListView
                jindiao_adapter = FengKongAdapter(context, 1, jin, ArrayList<TouHouManageItem>())
                listView.adapter = jindiao_adapter
                jindiao_adapter.add(GradeCheckBean.FengKongItem.FengKongInnerItem())

                var add_item = view.findViewById(R.id.add_item) as TextView
                add_item.setOnClickListener {
                    jindiao_adapter.add(GradeCheckBean.FengKongItem.FengKongInnerItem())
                }

            } else if (getItemViewType(position) == 0) {//tou  hou
                view = LayoutInflater.from(context).inflate(R.layout.fengkong_head_2, null)

                var std = view.findViewById(R.id.std2) as TextView
                std.text = initString(list.get(position).biaozhun!!)

                var listView = view.findViewById(R.id.mylist) as MyListView
                touhou_adapter = FengKongAdapter(context, 0, ArrayList<JinDiaoItem>(), touhou)
                listView.adapter = touhou_adapter

                touhou_adapter.addAll(list.get(position).data!!)
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
