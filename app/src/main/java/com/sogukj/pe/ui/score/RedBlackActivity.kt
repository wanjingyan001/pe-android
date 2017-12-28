package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EmployeeInteractBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_red_black.*
import kotlinx.android.synthetic.main.item_empty.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class RedBlackActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, RedBlackActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    var type = Extras.RED
    lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_red_black)

        setBack(true)
        setTitle("排行榜")
        toolbar?.setBackgroundColor(Color.TRANSPARENT)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#ffffff")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
            var toolbar_menu = this.findViewById(R.id.toolbar_menu) as TextView
            toolbar_menu.text = "黑榜"
            toolbar_menu.setOnClickListener {
                if (type == Extras.RED) {
                    type = Extras.BLACK
                    initView()
                } else if (type == Extras.BLACK) {
                    type = Extras.RED
                    initView()
                }
            }
        }

        SoguApi.getService(application)
                .grade_info()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapter = MyAdapter(context, this)
                            listview.adapter = adapter
                            adapter.red_or_black = type
                            adapter.notifyDataSetChanged()
                            //有数据
                            scroll.visibility = View.VISIBLE
                            empty.visibility = View.GONE
                        }
                        if (payload.payload == null || payload.payload?.size == 0) {
                            //暂无数据
                            scroll.visibility = View.GONE
                            empty.visibility = View.VISIBLE
                            tv_empty.visibility = View.GONE
                            toolbar_menu.text = ""
                            toolbar_menu.setOnClickListener { null }
                            toolbar_title.textColor = Color.parseColor("#ff000000")
                            root.backgroundColor = Color.parseColor("#ffffffff")
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

    fun initView() {
        if (type == Extras.RED) {
            toolbar_menu.text = "黑榜"
            //root.backgroundResource = R.drawable.red
            root.backgroundColor = Color.parseColor("#FFD3513C")
            icon_title.backgroundResource = R.drawable.hong
        } else if (type == Extras.BLACK) {
            toolbar_menu.text = "红榜"
            //root.backgroundResource = R.drawable.black
            root.backgroundColor = Color.parseColor("#FF0C162E")
            icon_title.backgroundResource = R.drawable.hei
        }
        adapter.red_or_black = type
        adapter.notifyDataSetChanged()
    }

    class MyAdapter(val context: Context, val data: ArrayList<EmployeeInteractBean>) : BaseAdapter() {

        var red_or_black = 0

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: InnerHolder? = null
            if (view == null) {
                holder = InnerHolder()
                var inflate = LayoutInflater.from(context)
                view = inflate.inflate(R.layout.red_black_item, null)
                holder?.title = view.findViewById(R.id.title) as TextView
                holder?.chakan = view.findViewById(R.id.chakan) as TextView
                var ll = view.findViewById(R.id.addContent) as LinearLayout


                var first = inflate.inflate(R.layout.red_black_inner, null) as LinearLayout
                holder?.list.add(inflateOneItem(first))
                ll.addView(first, 1)
                var second = inflate.inflate(R.layout.red_black_inner, null) as LinearLayout
                holder?.list.add(inflateOneItem(second))
                ll.addView(second, 2)
                var third = inflate.inflate(R.layout.red_black_inner, null) as LinearLayout
                holder?.list.add(inflateOneItem(third))
                ll.addView(third, 3)


                view.setTag(holder)
            } else {
                holder = view.tag as InnerHolder
            }


            holder?.title?.text = data[position].title
            var dataList = data[position].data
            try {
                if (red_or_black == Extras.RED) {
                    holder?.chakan?.textColor = Color.parseColor("#FFEA9C93")
                    var index = 0
                    fillOneItem(holder?.list[0], dataList?.get(index)!!, red_or_black, 0)
                    fillOneItem(holder?.list[1], dataList?.get(index + 1)!!, red_or_black, 1)
                    fillOneItem(holder?.list[2], dataList?.get(index + 2)!!, red_or_black, 2)
                } else if (red_or_black == Extras.BLACK) {
                    holder?.chakan?.textColor = Color.parseColor("#FFA0A4AA")
                    var index = dataList!!.size - 3
                    fillOneItem(holder?.list[0], dataList?.get(index)!!, red_or_black, 0)
                    fillOneItem(holder?.list[1], dataList?.get(index + 1)!!, red_or_black, 1)
                    fillOneItem(holder?.list[2], dataList?.get(index + 2)!!, red_or_black, 2)
                }
            } catch (e: Exception) {
                Log.e("数据没满三个", "数据没满三个")
            }


            holder?.chakan?.setOnClickListener {
                JiXiaoActivity.start(context, Extras.RED_BLACK, data[position])
            }
            return view!!
        }

        override fun getItem(position: Int): Any {
            return data.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return data.size
        }

        fun fillOneItem(item: InnerHolder.Item, itemData: EmployeeInteractBean.EmployeeItem, type: Int, index: Int) {
            if (type == Extras.RED) {
                item.seq?.text = ""
                if (index == 0) {
                    item.seq?.backgroundResource = R.drawable.no1
                } else if (index == 1) {
                    item.seq?.backgroundResource = R.drawable.no2
                } else if (index == 2) {
                    item.seq?.backgroundResource = R.drawable.no3
                }
            } else if (type == Extras.BLACK) {
                item.seq?.backgroundColor = Color.WHITE
                item.seq?.text = "${itemData.sort}"
            }
            //Glide.with(context).load(itemData.url).into(item.head_icon)
            item.name?.text = itemData.name
            item.depart?.text = itemData.department
            item.score?.text = itemData.grade_case
        }

        fun inflateOneItem(view: LinearLayout): InnerHolder.Item {
            var item = InnerHolder.Item()
            item.seq = view.findViewById(R.id.seq) as TextView
            //item.head_icon = view.findViewById(R.id.head_icon) as CircleImageView
            item.name = view.findViewById(R.id.name) as TextView
            item.depart = view.findViewById(R.id.depart) as TextView
            item.score = view.findViewById(R.id.score) as TextView
            return item
        }

        class InnerHolder {
            var title: TextView? = null
            var chakan: TextView? = null
            var addContent: LinearLayout? = null
            var list = ArrayList<Item>()

            class Item {
                var seq: TextView? = null
                //var head_icon: CircleImageView? = null
                var name: TextView? = null
                var depart: TextView? = null
                var score: TextView? = null
            }
        }
    }
}
