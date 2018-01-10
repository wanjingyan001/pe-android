package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EmployeeInteractBean
import com.sogukj.pe.bean.ScoreBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_leader.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class LeaderActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, LeaderActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    var role = 0
    var adjust = 0
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader)

        role = XmlDb.open(context).get(Extras.ROLE, "").toInt()//1=>领导班子 2=>部门负责人 3=>其他员工
        adjust = XmlDb.open(context).get(Extras.ADJUST, "").toInt()//领导班子是否可以显示调整项	1显示 0不显示

        setBack(true)
        setTitle("评分中心")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        if (role == 1 && adjust == 1) {
            //(ll_1_right.getChildAt(0) as ImageView).backgroundResource = R.drawable.yghpjg
            (ll_4_left.getChildAt(0) as ImageView).backgroundResource = R.drawable.khjg
        } else if (role == 1 && adjust == 0) {
            ll_3.visibility = View.GONE
            ll_3_left.visibility = View.GONE
            ll_3_right.visibility = View.GONE
            divider2.visibility = View.GONE
            //(ll_1_right.getChildAt(0) as ImageView).backgroundResource = R.drawable.yghpjg
            (ll_4_left.getChildAt(0) as ImageView).backgroundResource = R.drawable.khjg
        } else if (role == 2) {
            ll_3.visibility = View.GONE
            ll_3_left.visibility = View.GONE
            ll_3_right.visibility = View.GONE
            //ll_4.visibility = View.GONE
            //ll_4_left.visibility = View.GONE
            ll_4_right.visibility = View.GONE
            ll_2_right.visibility = View.INVISIBLE
            divider2.visibility = View.GONE
            //divider3.visibility = View.GONE
            divider4.visibility = View.GONE
            (ll_2_left.getChildAt(1) as TextView).text = "上级打分"
            (ll_1_left.getChildAt(1) as TextView).text = "员工互评打分"
            (ll_4_left.getChildAt(1) as TextView).text = "我的分数"
            (ll_4_left.getChildAt(0) as ImageView).backgroundResource = R.drawable.khjg
            ll_1_right.visibility = View.INVISIBLE
        } else if (role == 3) {
            ll_2.visibility = View.GONE
            ll_2_left.visibility = View.GONE
            ll_2_right.visibility = View.GONE
            ll_3.visibility = View.GONE
            ll_3_left.visibility = View.GONE
            ll_3_right.visibility = View.GONE
            //ll_4.visibility = View.GONE
            //ll_4_left.visibility = View.GONE
            ll_4_right.visibility = View.GONE
            divider1.visibility = View.GONE
            divider2.visibility = View.GONE
            //divider3.visibility = View.GONE
            divider4.visibility = View.GONE
            (ll_1_left.getChildAt(1) as TextView).text = "员工互评打分"
            ll_1_right.visibility = View.INVISIBLE
            (ll_4_left.getChildAt(1) as TextView).text = "我的分数"
            (ll_4_left.getChildAt(0) as ImageView).backgroundResource = R.drawable.khjg
        }

        ll_1_left.setOnClickListener {
            //留着。免得又要变
            GangWeiListActivity.start(context, Extras.TYPE_MANAGE)
        }

        ll_1_right.setOnClickListener {
            if (isLoading == true) {
                return@setOnClickListener
            }
            isLoading = true
            if (role == 1) {
                //员工互评考核结果不用传，领导打分详情页要传
                SoguApi.getService(application)
                        .grade_info()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                if (payload.payload == null || payload.payload!!.size == 0) {
                                    //暂无数据
                                    showToast("暂无数据")
                                } else {
                                    ScoreDetailActivity.start(context, Extras.TYPE_INTERACT, null)
                                }
                            } else
                                showToast(payload.message)
                            isLoading = false
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

        ll_2_left.setOnClickListener {
            GuanJianJiXiaoListActivity.start(context, Extras.TYPE_JIXIAO)
        }

        ll_2_right.setOnClickListener {
            if (isLoading == true) {
                return@setOnClickListener
            }
            isLoading = true
            SoguApi.getService(application)
                    .achievement()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            if (payload.payload == null || payload.payload!!.size == 0) {
                                showToast("暂无数据")
                            } else {
                                var bean = EmployeeInteractBean()
                                bean.data = payload.payload
                                JiXiaoActivity.start(context, Extras.JIXIAO, bean)
                            }
                        } else
                            showToast(payload.message)
                        isLoading = false
                    }, { e ->
                        Trace.e(e)
                        when (e) {
                            is JsonSyntaxException -> showToast("后台数据出错")
                            is UnknownHostException -> showToast("网络出错")
                            else -> showToast("未知错误")
                        }
                    })
        }

        ll_3_left.setOnClickListener {
            GuanJianJiXiaoListActivity.start(context, Extras.TYPE_TIAOZHENG)
        }

        ll_4_left.setOnClickListener {
            if (isLoading == true) {
                return@setOnClickListener
            }
            isLoading = true
            if (role == 1) {
                SoguApi.getService(application)
                        .pointRank()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                var data = payload.payload as ArrayList<ScoreBean>
                                if (data == null || data.size == 0) {
                                    showToast("暂无数据")
                                } else {
                                    ScoreListActivity.start(context, data)
                                }
                            } else {
                                showToast(payload.message)
                            }
                            isLoading = false
                        }, { e ->
                            Trace.e(e)
                            when (e) {
                                is JsonSyntaxException -> showToast("后台数据出错")
                                is UnknownHostException -> showToast("网络出错")
                                else -> showToast("未知错误")
                            }
                        })
            } else if (role == 2 || role == 3) {
                SoguApi.getService(application)
                        .showSumScore()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                if (payload.payload != null) {
                                    TotalScoreActivity.start(context, payload.payload!!)
                                } else {
                                    showToast("暂无数据")
                                }
                            } else
                                showToast(payload.message)
                            isLoading = false
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

        ll_4_right.setOnClickListener {
            if (isLoading == true) {
                return@setOnClickListener
            }
            isLoading = true
            SoguApi.getService(application)
                    .grade_info()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            if (payload.payload == null || payload.payload?.size == 0) {
                                //暂无数据
                                showToast("暂无数据")
                            } else {
                                RedBlackActivity.start(context, payload.payload!!)
                            }
                        } else
                            showToast(payload.message)
                        isLoading = false
                    }, { e ->
                        Trace.e(e)
                        when (e) {
                            is JsonSyntaxException -> showToast("后台数据出错")
                            is UnknownHostException -> showToast("网络出错")
                            else -> showToast("未知错误")
                        }
                    })
        }

        toolbar_menu.setOnClickListener {
            RuleActivity.start(context)
        }

        ll_4_new.setOnClickListener {
            PointProgressActivity.start(context)
        }

    }

    //下面是倒计时
    private val mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {
            if (msg.what == 0x001) {
                synchronized(this) {
                    var leftSec = msg.obj as Int
                    timeTick.text = "距离结束还有${formatSec(leftSec)}"
                    leftSec--
                    if (leftSec == -1) {
                        timeTick.text = "超时"
                    } else {
                        sendMessageDelayed(obtainMessage(0x001, leftSec), 1000)
                    }
                }
            }
        }
    }

    fun formatSec(sec: Int): String {
        var second = sec - sec / 60 * 60
        var minute = sec / 60 - sec / 60 / 60 * 60
        var hour = sec / 60 / 60 - sec / 60 / 60 / 24 * 24
        var day = sec / 60 / 60 / 24

        var str = ""
        if (day > 0) {
            str = "${str}${day}天"
        }
        if (hour > 0) {
            str = "${str}${hour}小时"
        }
        if (minute > 0) {
            str = "${str}${minute}分钟"
        }
        if (second > 0) {
            str = "${str}${second}秒"
        }

        return str
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeMessages(0x001)
    }

    override fun onResume() {
        super.onResume()
        mHandler.removeMessages(0x001)
        timeTick.text = ""
        SoguApi.getService(application)
                .getType()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            var TYPE = this.type as Int //-1=>隐藏入口 0=>未开启  1=>进入评分中心，2=>进入填写页面
                            if (TYPE == 1 || TYPE == 2) {
                                if (this.time != null) {
                                    Thread.sleep(1000)
                                    timeTick.visibility = View.VISIBLE
                                    mHandler.sendMessageDelayed(mHandler.obtainMessage(0x001, this.time), 0)
                                }
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
}
