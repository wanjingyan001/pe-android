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
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean.TouZiItem
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invest_manage.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

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

        setBack(true)
        setTitle("投资经理")
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
                            JudgeActivity.start(context, 3, 0)
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

            standard.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    upload.standard = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

            info.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    upload.info = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        }
    }
}
