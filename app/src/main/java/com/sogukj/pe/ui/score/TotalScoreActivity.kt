package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.view.SingleCircleScoreBoard
import com.sogukj.pe.view.TotalCircleScoreBoard
import kotlinx.android.synthetic.main.activity_total_score.*
import org.jetbrains.anko.textColor

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

        var tag = intent.getIntExtra(Extras.FLAG, 0)
        if (tag == 0) {
            ll_finish.visibility = View.GONE
            ll_unfinish.visibility = View.VISIBLE
            total.setTag()
        } else if (tag == 1) {
            ll_finish.visibility = View.VISIBLE
            ll_unfinish.visibility = View.GONE

            var timer = MyCountDownTimer(1000, 10, total)
            timer.start()

            var timer1 = MyCountDownTimer(1000, 10, single1)
            timer1.start()

            var timer2 = MyCountDownTimer(1000, 10, single2)
            timer2.start()
        }
    }

    //危险，不准确
    class MyCountDownTimer(var millisInFuture: Long, var countDownInterval: Long, var view: View) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            if (view is TotalCircleScoreBoard) {
                (view as TotalCircleScoreBoard).setDate(0, 108.99 / 100)
            } else {
                (view as SingleCircleScoreBoard).setDate(0, 42.00 / 100)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            if (view is TotalCircleScoreBoard) {
                (view as TotalCircleScoreBoard).setDate(millisUntilFinished.toInt() / 10, 108.99 / 100)
            } else {
                (view as SingleCircleScoreBoard).setDate(millisUntilFinished.toInt() / 10, 42.00 / 100)
            }
        }
    }

    companion object {
        fun start(ctx: Context?, type: Int) {
            ctx?.startActivity(Intent(ctx, TotalScoreActivity::class.java).putExtra(Extras.FLAG, type))
        }
    }
}
