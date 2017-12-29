package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.R
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.SingleCircleScoreBoard
import com.sogukj.pe.view.TotalCircleScoreBoard
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_total_score.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class TotalScoreActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_score)

        setBack(true)
        setTitle("年终考核中心")
        toolbar?.setBackgroundColor(Color.TRANSPARENT)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#FFFFFFFF")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        var bean = Store.store.getUser(context)
        bean?.let {
            Glide.with(context).load(it.url).into(icon)
            name.text = it.name
        }

        SoguApi.getService(application)
                .showSumScore()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            //-1=>还未打完分 1=>尚未完成打分，2=>已完成
                            var tag = this.status
                            if (tag == 2) {
                                ll_finish.visibility = View.VISIBLE
                                ll_unfinish.visibility = View.GONE

                                var timer = MyCountDownTimer(1000, 10, total, total_grade!!.toDouble())
                                timer.start()

                                var timer1 = MyCountDownTimer(1000, 10, single1, achieve_check!!.toDouble())
                                timer1.start()

                                var timer2 = MyCountDownTimer(1000, 10, single2, resumption!!.toDouble())
                                timer2.start()

                                var timer3 = MyCountDownTimer(1000, 10, single3, adjust!!.toDouble())
                                timer3.start()
                            } else {
                                ll_finish.visibility = View.GONE
                                ll_unfinish.visibility = View.VISIBLE
                                total.setTag()
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

    //危险，不准确
    class MyCountDownTimer(var millisInFuture: Long, var countDownInterval: Long, var view: View, var score: Double) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            if (view is TotalCircleScoreBoard) {
                (view as TotalCircleScoreBoard).setDate(0, score / 100, (score / 100).toFloat())
            } else {
                (view as SingleCircleScoreBoard).setDate(0, score / 100, (score / 100).toFloat())
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            if (view is TotalCircleScoreBoard) {
                (view as TotalCircleScoreBoard).setDate(millisUntilFinished.toInt() / 10, score / 100, (score / 100).toFloat())
            } else {
                (view as SingleCircleScoreBoard).setDate(millisUntilFinished.toInt() / 10, score / 100, (score / 100).toFloat())
            }
        }
    }

    companion object {
        fun start(ctx: Context?) {
            ctx?.startActivity(Intent(ctx, TotalScoreActivity::class.java))
        }
    }
}
