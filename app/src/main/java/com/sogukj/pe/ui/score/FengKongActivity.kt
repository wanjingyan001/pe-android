package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_feng_kong.*
import org.jetbrains.anko.textColor
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.view.LayoutInflater
import android.widget.*
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.bean.JinDiaoItem
import com.sogukj.pe.bean.TouHouManageItem
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.Bean
import com.sogukj.pe.view.FengKongAdapter
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feng_kong)

        setBack(true)
        setTitle("考核评分")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        val spannableString = SpannableString("评分标准:每个参与尽调项目得20分;最高分数:120分")
        //Spanned.SPAN_INCLUSIVE_EXCLUSIVE 从起始下标到终了下标，包括起始下标
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#FFE95C4A")), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#FF323232")), 5, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        std1.text = spannableString

        //评分标准：优秀 101~120分/良好81~100分/合格61~80分/不称职0~60分
        val spannable1 = SpannableString("评分标准:优秀101~120分/良好81~100分/合格61~80分/不称职0~60分")
        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FFE95C4A")), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FF323232")), 5, spannable1.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        std2.text = spannable1

        var adapter = FengKongAdapter(context)
        list.adapter = adapter

        adapter.addAll(arrayListOf(Bean(), Bean(), Bean()))

        inflater = LayoutInflater.from(context)

        add_item.setOnClickListener {
            var item = inflater.inflate(R.layout.fengkong_item, null) as LinearLayout
            items.addView(item)
        }

        btn_commit.setOnClickListener {
            preparePrams()
            SoguApi.getService(application)
                    .risk_add(jin, touhou)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {

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

    lateinit var inflater: LayoutInflater

    fun preparePrams() {

    }
}
