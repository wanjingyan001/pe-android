package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_feng_kong.*
import org.jetbrains.anko.textColor
import android.databinding.adapters.TextViewBindingAdapter.setText
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText


class FengKongActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity) {
            val intent = Intent(ctx, FengKongActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

//    //radii单位是px
//    fun setRadii(view: View, radii: Float) {
//        var array = floatArrayOf(radii, radii, radii, radii, radii, radii, radii, radii)
//        var gd = GradientDrawable()//创建drawable
//        gd.cornerRadii = array
//        //gd.setStroke(Utils.dpToPx(context, 1), Color.argb(54, 238, 240, 245))
//        gd.setStroke(Utils.dpToPx(context, 1), Color.BLACK)
//        view.setBackgroundDrawable(gd)
//    }

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
    }

    class FengKongAdapter(val context: Context) : BaseAdapter() {

        val datalist = ArrayList<Bean>()

        fun addAll(list: ArrayList<Bean>) {
            datalist.addAll(list)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: Holder? = null
            if (view == null) {
                holder = Holder()
                view = LayoutInflater.from(context).inflate(R.layout.item_fengkong, null)
                holder?.title = view.findViewById(R.id.item_title) as TextView
                holder?.content = view.findViewById(R.id.item_content) as EditText
                view.setTag(holder)
            } else {
                holder = view.tag as Holder
            }
            return view!!
        }

        override fun getItem(position: Int): Any {
            return datalist.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return datalist.size
        }
    }

    class Bean {

    }

    class Holder {
        var title: TextView? = null
        var content: EditText? = null
    }
}
