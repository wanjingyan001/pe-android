package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.google.gson.JsonSyntaxException
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.JinDiaoItem
import com.sogukj.pe.bean.TouHouManageItem
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
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
            if (canClick == false) {
                return@setOnClickListener
            }

            MaterialDialog.Builder(context)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确定要提交此标准?")
                    .onPositive { materialDialog, dialogAction ->
                        var jin__ = ArrayList<HashMap<String, String>>()
                        for (item in jin) {
                            var jin_item = HashMap<String, String>()
                            jin_item.put("target", item.title!!)
                            jin_item.put("info", item.info!!)
                            jin__.add(jin_item)
                        }

                        var touhou__ = ArrayList<HashMap<String, String>>()
                        for (item in touhou) {
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
                                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }, { e ->
                                    Trace.e(e)
                                    ToastError(e)
                                })
                    }
                    .positiveText("确定")
                    .negativeText("取消")
                    .show()
        }

        SoguApi.getService(application)
                .check(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            fengkong?.let {
                                //jin diao
                                var std = findViewById(R.id.std1) as TextView
                                std.text = initString(it.get(0).biaozhun!!)

                                var listView = findViewById(R.id.myList1) as RecyclerView
                                var jindiao_adapter = RecyclerAdapter<GradeCheckBean.FengKongItem.FengKongInnerItem>(context, { _adapter, parent, t ->
                                    JinDiaoHolder(_adapter.getView(R.layout.fengkong_item, parent))
                                })
                                val layoutManager = LinearLayoutManager(context)
                                layoutManager.orientation = LinearLayoutManager.VERTICAL
                                listView.layoutManager = layoutManager
                                listView.adapter = jindiao_adapter
                                jindiao_adapter.dataList.add(GradeCheckBean.FengKongItem.FengKongInnerItem())
                                jindiao_adapter.notifyDataSetChanged()

                                var add_item = findViewById(R.id.add_item) as TextView
                                add_item.setOnClickListener {
                                    jin.clear()
                                    isJinDiaoReady = false
                                    observable_List_jindiao_zb.clear()
                                    observable_List_jindiao_cond.clear()
                                    jindiao_adapter.dataList.add(GradeCheckBean.FengKongItem.FengKongInnerItem())
                                    jindiao_adapter.notifyDataSetChanged()
                                    max_jindiao = jindiao_adapter.dataList.size
                                }
                                max_jindiao = 1

                                //tou hou
                                var std2 = findViewById(R.id.std2) as TextView
                                std2.text = initString(it.get(1).biaozhun!!)

                                var listView2 = findViewById(R.id.mylist2) as RecyclerView
                                var touhou_adapter = RecyclerAdapter<GradeCheckBean.FengKongItem.FengKongInnerItem>(context, { _adapter, parent, t ->
                                    TouHouHolder(_adapter.getView(R.layout.item_fengkong, parent))
                                })
                                val layoutManager1 = LinearLayoutManager(context)
                                layoutManager1.orientation = LinearLayoutManager.VERTICAL
                                listView2.layoutManager = layoutManager1
                                listView2.adapter = touhou_adapter

                                touhou_adapter.dataList.addAll(it.get(1).data!!)
                                touhou_adapter.notifyDataSetChanged()
                                max_touhou = it.get(1).data!!.size
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    fun initString(str: String): SpannableString {
        val spannable1 = SpannableString("评分标准:${str}")
        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FFE95C4A")), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FF323232")), 5, spannable1.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable1
    }

    var max_touhou = 0
    val observable_List_touhou = ArrayList<Observable<String>>()
    var touhou = ArrayList<TouHouManageItem>()
    var isTouHouReady = false

    var max_jindiao = 0
    val observable_List_jindiao_zb = ArrayList<Observable<String>>()
    val observable_List_jindiao_cond = ArrayList<Observable<String>>()
    var jin = ArrayList<JinDiaoItem>()
    var isJinDiaoReady = false

    var canClick = false

    /**
     * 0--touhou
     */
    inner class TouHouHolder(view: View)
        : RecyclerHolder<GradeCheckBean.FengKongItem.FengKongInnerItem>(view) {

        var title = view.findViewById(R.id.item_title) as TextView
        var content = view.findViewById(R.id.item_content) as EditText

        override fun setData(view: View, data: GradeCheckBean.FengKongItem.FengKongInnerItem, position: Int) {
            Log.e("TouHouHolder", "TouHouHolder")
            title.text = data.zhibiao
            content.filters = Utils.getFilter(context)
            var item = TouHouManageItem()
            item.performance_id = data.performance_id
            item.info = ""
            touhou.add(item)
            var obser = RxTextView.textChanges(content).map(object : Function<CharSequence, String> {
                override fun apply(t: CharSequence): String {
                    return t.toString()
                }
            })
            observable_List_touhou.add(obser)

            if (max_touhou == observable_List_touhou.size) {
                Observable.combineLatest(observable_List_touhou, object : Function<Array<Any>, Boolean> {
                    override fun apply(str: Array<Any>): Boolean {
                        for (item in 0 until touhou.size) {
                            touhou[item].info = str[item] as String
                        }
                        return true
                    }
                }).subscribe(object : Consumer<Boolean> {
                    override fun accept(t: Boolean) {

                        var flag = false
                        for (item in touhou) {
                            if (item.info.toString() == "") {
                                flag = true
                                break
                            }
                        }

                        if (flag == false) {
                            isTouHouReady = true
                        }

                        if (flag == false && isJinDiaoReady == true) {
                            btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                            canClick = true
                        } else {
                            btn_commit.setBackgroundColor(Color.parseColor("#FFD9D9D9"))
                        }
                    }
                })
            }
        }
    }

    /**
     * 1 jindiao
     */
    inner class JinDiaoHolder(view: View)
        : RecyclerHolder<GradeCheckBean.FengKongItem.FengKongInnerItem>(view) {

        var zhibiao = view.findViewById(R.id.zhibiao) as EditText
        var condition = view.findViewById(R.id.condi) as EditText

        override fun setData(view: View, data: GradeCheckBean.FengKongItem.FengKongInnerItem, position: Int) {
            Log.e("JinDiaoHolder", "JinDiaoHolder")
            zhibiao.filters = Utils.getFilter(context)
            condition.filters = Utils.getFilter(context)
            var item = JinDiaoItem()
            item.info = ""
            item.title = ""
            jin.add(item)

            var obser = RxTextView.textChanges(zhibiao).map(object : Function<CharSequence, String> {
                override fun apply(t: CharSequence): String {
                    return t.toString()
                }
            })
            observable_List_jindiao_zb.add(obser)

            var obser1 = RxTextView.textChanges(condition).map(object : Function<CharSequence, String> {
                override fun apply(t: CharSequence): String {
                    return t.toString()
                }
            })
            observable_List_jindiao_cond.add(obser1)

            if (max_jindiao == observable_List_jindiao_zb.size) {
                Observable.combineLatest(observable_List_jindiao_zb, object : Function<Array<Any>, Boolean> {
                    override fun apply(str: Array<Any>): Boolean {
                        for (item in 0 until jin.size) {
                            jin[item].title = str[item] as String
                        }
                        return true
                    }
                }).subscribe(object : Consumer<Boolean> {
                    override fun accept(t: Boolean) {
                        // TODO
                        Observable.combineLatest(observable_List_jindiao_cond, object : Function<Array<Any>, Boolean> {
                            override fun apply(str: Array<Any>): Boolean {
                                for (item in 0 until jin.size) {
                                    jin[item].info = str[item] as String
                                }
                                return true
                            }
                        }).subscribe(object : Consumer<Boolean> {
                            override fun accept(t: Boolean) {

                                var flag = false
                                for (item in jin) {
                                    if (item.title.toString() == "" || item.info.toString() == "") {
                                        flag = true
                                        break
                                    }
                                }

                                if (flag == false) {
                                    isJinDiaoReady = true
                                }

                                if (flag == false && isTouHouReady == true) {
                                    btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                                    canClick = true
                                } else {
                                    btn_commit.setBackgroundColor(Color.parseColor("#FFD9D9D9"))
                                }
                            }
                        })
                    }
                })
            }
        }
    }
}
