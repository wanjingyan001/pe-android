package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean.TouZiItem
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invest_manage.*
import kotlinx.android.synthetic.main.header.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException
import io.reactivex.functions.Function

class InvestManageActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, InvestManageActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    lateinit var invest_adapter: RecyclerAdapter<TouZiItem>
    var dataList = ArrayList<TouZiUpload>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invest_manage)

        var bean = Store.store.getUser(context)
        bean?.let {
            Glide.with(context).load(it.url).into(icon)
            name.text = it.name
            depart.text = it.depart_name
            position.text = it.position
        }

        setBack(true)
        setTitle("投资经理评分标准填写")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        invest_adapter = RecyclerAdapter<TouZiItem>(context, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.item_rate_title, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        invest_list.layoutManager = layoutManager
        //invest_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        invest_list.adapter = invest_adapter

        SoguApi.getService(application)
                .check(4)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            touzhi?.let {
                                it?.forEach {
                                    invest_adapter.dataList.add(it)
                                    MAX += it.data!!.size
                                }
                                invest_adapter.notifyDataSetChanged()
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

    var MAX = 0

    inner class ProjectHolder(view: View)
        : RecyclerHolder<TouZiItem>(view) {

        var head_title = convertView.findViewById(R.id.head_title) as TextView
        var list = convertView.findViewById(R.id.listview) as RecyclerView
        lateinit var inner_adapter: RecyclerAdapter<TouZiItem.TouZiInnerItem>

        override fun setData(view: View, data: TouZiItem, position: Int) {
            head_title.text = data.title
            //invest_item
            inner_adapter = RecyclerAdapter<TouZiItem.TouZiInnerItem>(context, { _adapter, parent, t ->
                ProjectHolderNoTitle(_adapter.getView(R.layout.invest_item, parent))
            })
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            list.layoutManager = layoutManager
            list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
            list.adapter = inner_adapter

            data.data?.forEach {
                inner_adapter.dataList.add(it)
            }
            inner_adapter.notifyDataSetChanged()
        }
    }

    val observable_List_standard = ArrayList<Observable<String>>()
    val observable_List_info = ArrayList<Observable<String>>()

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<TouZiItem.TouZiInnerItem>(view) {

        var zhibiao = convertView.findViewById(R.id.zhibiao) as TextView
        var standard = convertView.findViewById(R.id.standard) as EditText
        var info = convertView.findViewById(R.id.info) as EditText

        override fun setData(view: View, data: TouZiItem.TouZiInnerItem, position: Int) {
            zhibiao.text = data.zhibiao

            var upload = TouZiUpload()
            upload.performance_id = data.performance_id
            upload.standard = ""
            upload.info = ""
            dataList.add(upload)

            var obser = RxTextView.textChanges(standard).map(object : Function<CharSequence, String> {
                override fun apply(t: CharSequence): String {
                    //upload.standard = t.text().toString()
                    return t.toString()
                }
            })
            observable_List_standard.add(obser)

            var obser2 = RxTextView.textChanges(info).map(object : Function<CharSequence, String> {
                override fun apply(t: CharSequence): String {
                    //upload.info = t.text().toString()
                    return t.toString()
                }
            })
            observable_List_info.add(obser2)

            if (observable_List_info.size == MAX) {
                Observable.combineLatest(observable_List_standard, object : Function<Array<Any>, Boolean> {
                    override fun apply(str: Array<Any>): Boolean {
                        for (item in 0 until dataList.size) {
                            dataList[item].standard = str[item] as String
                        }
                        return true
                    }
                }).subscribe(object : Consumer<Boolean> {
                    override fun accept(t: Boolean) {
                        // TODO
                        Observable.combineLatest(observable_List_info, object : Function<Array<Any>, Boolean> {
                            override fun apply(str: Array<Any>): Boolean {
                                for (item in 0 until dataList.size) {
                                    dataList[item].info = str[item] as String
                                }
                                return true
                            }
                        }).subscribe(object : Consumer<Boolean> {
                            override fun accept(t: Boolean) {

                                var flag = false
                                for (item in dataList) {
                                    if (item.standard.toString() == "" || item.info.toString() == "") {
                                        flag = true
                                        break
                                    }
                                }

                                if (flag) {
                                    btn_commit.setBackgroundColor(Color.parseColor("#FFD9D9D9"))
                                } else {
                                    btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                                }

                                btn_commit.setOnClickListener {

                                    for (item in dataList) {
                                        if (item.standard.toString() == "" || item.info.toString() == "") {
                                            return@setOnClickListener
                                        }
                                    }

                                    var data = ArrayList<HashMap<String, String>>()
                                    for (item in dataList) {
                                        val inner = HashMap<String, String>()
                                        inner.put("performance_id", "${item.performance_id}")
                                        inner.put("standard", item.standard!!)
                                        inner.put("info", item.info!!)
                                        data.add(inner)
                                    }

                                    val params = HashMap<String, ArrayList<HashMap<String, String>>>()
                                    params.put("data", data)
                                    SoguApi.getService(application)
                                            .invest_add(params)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe({ payload ->
                                                if (payload.isOk) {
                                                    GangWeiListActivity.start(context, Extras.TYPE_EMPLOYEE)
                                                    //JudgeActivity.start(context, 3, 100)
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
                            }
                        })
                    }
                })
            }
        }
    }
}
