package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import io.reactivex.functions.Function
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.TemplateBean
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.util.MyGlideUrl
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_template.*
import kotlinx.android.synthetic.main.header.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class TemplateActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, TemplateActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)

        var bean = Store.store.getUser(context)
        bean?.let {
            var icon = findViewById(R.id.icon) as CircleImageView
            //Glide.with(context).load(it.url).into(icon)
            if (it.url.isNullOrEmpty()) {
                val ch = it.name?.first()
                icon.setChar(ch)
            } else {
                Glide.with(context)
                        .load(MyGlideUrl(it.url))
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(icon)
            }
            name.text = it.name
            depart.text = it.depart_name
            position.text = it.position
        }

        setBack(true)
        setTitle("关键绩效指标填写界面")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        //绩效
        jxadapter = RecyclerAdapter<TemplateBean.InnerItem>(context, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.template_item, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        templist.layoutManager = layoutManager
        templist.adapter = jxadapter

        //jiafen
        add_adapter = RecyclerAdapter<TemplateBean.InnerItem>(context, { _adapter, parent, t ->
            ProjectItemHolder(_adapter.getView(R.layout.add_min_item, parent))
        })
        val layoutManager1 = LinearLayoutManager(context)
        layoutManager1.orientation = LinearLayoutManager.VERTICAL
        add_list.layoutManager = layoutManager1
        add_list.adapter = add_adapter

        //jianfen
        minus_adapter = RecyclerAdapter<TemplateBean.InnerItem>(context, { _adapter, parent, t ->
            ProjectItemHolder(_adapter.getView(R.layout.add_min_item, parent))
        })
        val layoutManager2 = LinearLayoutManager(context)
        layoutManager2.orientation = LinearLayoutManager.VERTICAL
        minus_list.layoutManager = layoutManager2
        minus_list.adapter = minus_adapter

        SoguApi.getService(application)
                .showWrite()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            jx?.forEach {
                                jxadapter.dataList.add(it)
                            }
                            jxadapter.notifyDataSetChanged()

                            jf?.forEach {
                                add_adapter.dataList.add(it)
                            }
                            add_adapter.notifyDataSetChanged()

                            kf?.forEach {
                                minus_adapter.dataList.add(it)
                            }
                            minus_adapter.notifyDataSetChanged()

                            MAX = jxadapter.dataList.size + add_adapter.dataList.size + minus_adapter.dataList.size
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    var MAX = 0
    val observable_List = ArrayList<Observable<String>>()
    var dataList = ArrayList<TouZiUpload>()

    lateinit var jxadapter: RecyclerAdapter<TemplateBean.InnerItem>
    lateinit var add_adapter: RecyclerAdapter<TemplateBean.InnerItem>
    lateinit var minus_adapter: RecyclerAdapter<TemplateBean.InnerItem>

    inner class ProjectHolder(view: View)
        : RecyclerHolder<TemplateBean.InnerItem>(view) {

        var head_title = convertView.findViewById(R.id.head_title) as TextView
        var zhibiao = convertView.findViewById(R.id.zhibiao) as TextView
        var condi = convertView.findViewById(R.id.condi) as EditText

        override fun setData(view: View, data: TemplateBean.InnerItem, position: Int) {
            head_title.text = data.name
            zhibiao.text = data.info
            condi.filters = Utils.getFilter(context)
            var upload = TouZiUpload()
            upload.performance_id = data.performance_id
            upload.actual = ""
            dataList.add(upload)

            var obser = RxTextView.textChanges(condi).map(object : Function<CharSequence, String> {
                override fun apply(t: CharSequence): String {
                    return t.toString()
                }
            })
            observable_List.add(obser)
        }
    }

    inner class ProjectItemHolder(view: View)
        : RecyclerHolder<TemplateBean.InnerItem>(view) {

        var title = convertView.findViewById(R.id.title) as TextView
        var content = convertView.findViewById(R.id.content) as EditText

        override fun setData(view: View, data: TemplateBean.InnerItem, position: Int) {
            title.text = data.name
            content.filters = Utils.getFilter(context)
            var upload = TouZiUpload()
            upload.performance_id = data.performance_id
            upload.actual = ""
            dataList.add(upload)

            var obser = RxTextView.textChanges(content).map(object : Function<CharSequence, String> {
                override fun apply(t: CharSequence): String {
                    return t.toString()
                }
            })
            observable_List.add(obser)

            if (observable_List.size == MAX) {
                Observable.combineLatest(observable_List, object : Function<Array<Any>, Boolean> {
                    override fun apply(str: Array<Any>): Boolean {
                        for (item in 0 until dataList.size) {
                            dataList[item].actual = str[item] as String
                        }
                        return true
                    }
                }).subscribe(object : Consumer<Boolean> {
                    override fun accept(t: Boolean) {
                        // TODO
                        var flag = false
                        for (item in dataList) {
                            if (item.actual.toString() == "") {
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
                            if (flag) {
                                return@setOnClickListener
                            }
                            MaterialDialog.Builder(context)
                                    .theme(Theme.LIGHT)
                                    .title("提示")
                                    .content("确定要提交此指标?")
                                    .onPositive { materialDialog, dialogAction ->
                                        upload()
                                    }
                                    .positiveText("确定")
                                    .negativeText("取消")
                                    .show()
                        }
                    }
                })
            }
        }
    }

    fun upload() {
        var data = ArrayList<HashMap<String, String>>()
        for (item in dataList) {
            val inner = HashMap<String, String>()
            inner.put("performance_id", "${item.performance_id}")
            inner.put("actual", item.actual!!)
            data.add(inner)
        }

        val params = HashMap<String, ArrayList<HashMap<String, String>>>()
        params.put("ae", data)
        SoguApi.getService(application)
                .writeAdd(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        //GangWeiListActivity.start(context, Extras.TYPE_EMPLOYEE)
                        LeaderActivity.start(context)
                        finish()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }
}
