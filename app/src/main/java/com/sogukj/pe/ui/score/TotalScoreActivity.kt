package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.view.SingleCircleScoreBoard
import com.sogukj.pe.view.TotalCircleScoreBoard
import kotlinx.android.synthetic.main.activity_total_score.*

class TotalScoreActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_score)

        setBack(true)
        setTitle("TotalScoreActivity")

        var timer = MyCountDownTimer(1000, 10, total)
        timer.start()

        var timer1 = MyCountDownTimer(1000, 10, single1)
        timer1.start()

        var timer2 = MyCountDownTimer(1000, 10, single2)
        timer2.start()
    }

    //危险，不准确
    class MyCountDownTimer(var millisInFuture: Long, var countDownInterval: Long, var view: View) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            if (view is TotalCircleScoreBoard) {
                (view as TotalCircleScoreBoard).setDate(0, 99.00 / 100)
            } else {
                (view as SingleCircleScoreBoard).setDate(0, 44.00 / 100)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            if (view is TotalCircleScoreBoard) {
                (view as TotalCircleScoreBoard).setDate(millisUntilFinished.toInt() / 10, 99.00 / 100)
            } else {
                (view as SingleCircleScoreBoard).setDate(millisUntilFinished.toInt() / 10, 44.00 / 100)
            }
        }
    }

    companion object {
        fun start(ctx: Context?) {
            ctx?.startActivity(Intent(ctx, TotalScoreActivity::class.java))
        }
    }
}
