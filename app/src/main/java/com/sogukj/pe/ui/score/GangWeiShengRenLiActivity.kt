package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.JobPageBean
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gang_wei_sheng_ren_li.*
import kotlinx.android.synthetic.main.header.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class GangWeiShengRenLiActivity : ToolbarActivity() {

    companion object {
        //id person.id, isShow-- 是否展示页面  true为展示页面，false是打分界面
        fun start(ctx: Context?, person: GradeCheckBean.ScoreItem, isShow: Boolean) {
            val intent = Intent(ctx, GangWeiShengRenLiActivity::class.java)
            intent.putExtra(Extras.DATA, person)
            intent.putExtra(Extras.FLAG, isShow)
            ctx?.startActivity(intent)
        }
    }

    lateinit var sub_adapter: RecyclerAdapter<JobPageBean.PageItem>
    var id = -1
    var isShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gang_wei_sheng_ren_li)

        toolbar_menu.setOnClickListener {
            RuleActivity.start(context)
        }

        var person = intent.getSerializableExtra(Extras.DATA) as GradeCheckBean.ScoreItem
        id = person.user_id!!
        person?.let {
            Glide.with(context).load(it.url).into(icon)
            name.text = it.name
            depart.text = it.department
            position.text = it.position
        }

        isShow = intent.getBooleanExtra(Extras.FLAG, false)

        setBack(true)
        setTitle("岗位胜任力考核评分")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        sub_adapter = RecyclerAdapter<JobPageBean.PageItem>(context, { _adapter, parent, t ->
            ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list.layoutManager = layoutManager
        rate_list.addItemDecoration(SpaceItemDecoration(30))
        rate_list.adapter = sub_adapter
        doRequest()
    }

    var pinfen = ArrayList<JobPageBean.PFBZ>()

    fun initPinFen() {

    }

    fun doRequest() {
        SoguApi.getService(application)
                .showJobPage(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            data?.forEach {
                                sub_adapter.dataList.add(it)
                            }
                            sub_adapter.notifyDataSetChanged()

                            pfbz?.forEach {
                                pinfen.add(it)
                            }
                        }
                        if (isShow) {
                            tv_socre.text = payload.total as String
                            btn_commit.visibility = View.GONE
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

    val observable_List = ArrayList<Observable<Int>>()
    val weight_list = ArrayList<Int>()
    var dataList = ArrayList<TouZiUpload>()

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<JobPageBean.PageItem>(view) {

        var bar = convertView.findViewById(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById(R.id.text) as TextView
        var title = convertView.findViewById(R.id.title) as TextView
        var sub_title = convertView.findViewById(R.id.subtitle) as TextView
        var desc = convertView.findViewById(R.id.desc) as TextView
        var lll = convertView.findViewById(R.id.lll) as LinearLayout

        override fun setData(view: View, data: JobPageBean.PageItem, position: Int) {

            title.text = data.name
            lll.visibility = View.GONE

            if (isShow) {
                var score = data.score?.toInt()!!
                bar.progress = score
                if (score >= 101 && score <= 120) {
                    bar.progressDrawable = context.resources.getDrawable(R.drawable.pb_a)
                } else if (score >= 81 && score <= 100) {
                    bar.progressDrawable = context.resources.getDrawable(R.drawable.pb_b)
                } else if (score >= 61 && score <= 80) {
                    bar.progressDrawable = context.resources.getDrawable(R.drawable.pb_c)
                } else if (score >= 0 && score <= 60) {
                    bar.progressDrawable = context.resources.getDrawable(R.drawable.pb_d)
                }
                judge.setText(data.score)
                judge.setTextColor(Color.parseColor("#ffa0a4aa"))
                judge.setTextSize(16f)
                judge.setBackgroundDrawable(null)
            } else {
                var obser = TextViewClickObservable(context, judge, bar)
                observable_List.add(obser)

                weight_list.add(data.weight!!.toInt())

                var upload = TouZiUpload()
                upload.performance_id = data.id!!.toInt()
                upload.type = data.type
                dataList.add(upload)

                if (observable_List.size == sub_adapter.dataList.size) {
                    Observable.combineLatest(observable_List, object : Function<Array<Any>, Double> {
                        override fun apply(str: Array<Any>): Double {
                            var result = 0.0
                            var date = ArrayList<Int>()//每项分数
                            for (ites in str) {
                                date.add(ites as Int)
                            }
                            for (i in weight_list.indices) {
                                dataList[i].score = date[i]
                                var single = date[i].toDouble() * weight_list[i] / 100
                                result += single
                            }
                            return result//isEmailValid(str[0].toString()) && isPasswordValid(str[1].toString())
                        }
                    }).subscribe(object : Consumer<Double> {
                        override fun accept(t: Double) {
                            tv_socre.text = "${String.format("%1$.2f", t)}"
                            btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                            btn_commit.setOnClickListener {
                                MaterialDialog.Builder(context)
                                        .theme(Theme.LIGHT)
                                        .title("提示")
                                        .content("确定要提交分数?")
                                        .onPositive { materialDialog, dialogAction ->
                                            upload(t)
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
    }

    fun upload(result: Double) {

        var data = ArrayList<HashMap<String, Int>>()
        for (item in dataList) {
            val inner = HashMap<String, Int>()
            inner.put("performance_id", item.performance_id!!)
            inner.put("score", item.score!!)
            inner.put("type", item.type!!)
            data.add(inner)
        }

        val params = HashMap<String, Any>()
        params.put("data", data)
        params.put("user_id", id)
        params.put("type", 2)
        params.put("total", result)

        SoguApi.getService(application)
                .giveGrade(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
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
