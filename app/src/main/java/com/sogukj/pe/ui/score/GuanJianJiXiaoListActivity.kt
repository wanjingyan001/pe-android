package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_guan_jian_ji_xiao_list.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class GuanJianJiXiaoListActivity : ToolbarActivity() {

    companion object {
        // TYPE_TIAOZHENG    TYPE_JIXIAO
        fun start(ctx: Context?, type: Int) {
            val intent = Intent(ctx, GuanJianJiXiaoListActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<GradeCheckBean.ScoreItem>

    var currentIndex = 0
    var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guan_jian_ji_xiao_list)

        type = intent.getIntExtra(Extras.TYPE, 0)

        setBack(true)
        if (type == Extras.TYPE_JIXIAO) {
            setTitle("关键绩效考核评价")
        } else {
            setTitle("调整项考核评价")
        }
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        if (currentIndex == 0) {
            tag_1.visibility = View.VISIBLE
            tag_2.visibility = View.VISIBLE
            tag_3.visibility = View.GONE
            tag_4.visibility = View.VISIBLE
        } else if (currentIndex == 1) {
            tag_1.visibility = View.VISIBLE
            tag_2.visibility = View.VISIBLE
            tag_3.visibility = View.VISIBLE
            tag_4.visibility = View.VISIBLE
        }

        list_tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    tag_1.visibility = View.VISIBLE
                    tag_2.visibility = View.VISIBLE
                    tag_3.visibility = View.GONE
                    tag_4.visibility = View.VISIBLE
                } else if (tab.position == 1) {
                    tag_1.visibility = View.VISIBLE
                    tag_2.visibility = View.VISIBLE
                    tag_3.visibility = View.VISIBLE
                    tag_4.visibility = View.VISIBLE
                }
                if (tab.position == 0) {
                    currentIndex = 0
                    loadData()
                } else if (tab.position == 1) {
                    currentIndex = 1
                    loadData()
                }
            }
        })

        adapter = RecyclerAdapter<GradeCheckBean.ScoreItem>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_judge, parent) as LinearLayout
            object : RecyclerHolder<GradeCheckBean.ScoreItem>(convertView) {

                val tvTag1 = convertView.findViewById(R.id.tag1) as TextView
                val tvTag2 = convertView.findViewById(R.id.tag2) as TextView
                val tvTag3 = convertView.findViewById(R.id.tag3) as TextView
                val tvTag4 = convertView.findViewById(R.id.tag4) as TextView

                override fun setData(view: View, data: GradeCheckBean.ScoreItem, position: Int) {
                    if (currentIndex == 1) {
                        tvTag3.textColor = Color.parseColor("#FFA1CEA9")
                    } else if (currentIndex == 0) {
                        tvTag3.textColor = Color.parseColor("#FFCEA1A1")
                    }
                    if (currentIndex == 0) {
                        tvTag1.visibility = View.VISIBLE
                        tvTag2.visibility = View.VISIBLE
                        tvTag3.visibility = View.VISIBLE
                        tvTag4.visibility = View.GONE
                    } else if (currentIndex == 1) {
                        tvTag1.visibility = View.VISIBLE
                        tvTag2.visibility = View.VISIBLE
                        tvTag3.visibility = View.VISIBLE
                        tvTag4.visibility = View.VISIBLE
                    }
                    tvTag1.text = data.name
                    tvTag2.text = data.department
                    tvTag3.text = data.position
                    tvTag4.text = data.grade_date
                }
            }
        })
        //type: Int? = null //1=>其他模版 2=>风控部模版 3=>投资部模版
        adapter.onItemClick = { v, p ->
            var person = adapter.dataList.get(p)
//            if (person.type == 3) {
//                if (currentIndex == 0) {
//                    RateActivity.start(context, person, false)
//                } else if (currentIndex == 1) {
//                    RateActivity.start(context, person, true)
//                }
//            } else if (person.type == 2) {
//                if (currentIndex == 0) {
//                    RateActivity.start(context, person, false)
//                } else if (currentIndex == 1) {
//                    RateActivity.start(context, person, true)
//                }
//            } else if (person.type == 1) {
//                if (currentIndex == 0) {
//                    RateActivity.start(context, person, false)
//                } else if (currentIndex == 1) {
//                    RateActivity.start(context, person, true)
//                }
//            }
            if (currentIndex == 0) {
                RateActivity.start(context, person, false, type)
            } else if (currentIndex == 1) {
                RateActivity.start(context, person, true, type)
            }
        }

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.addItemDecoration(SpaceItemDecoration(10))
        list.adapter = adapter
    }

    fun loadData() {
        if (currentIndex == 0) {
            adapter.dataList.clear()
            adapter.dataList.addAll(unfinish)
            adapter.notifyDataSetChanged()
        } else if (currentIndex == 1) {
            adapter.dataList.clear()
            adapter.dataList.addAll(finish)
            adapter.notifyDataSetChanged()
        }
    }

    var unfinish = ArrayList<GradeCheckBean.ScoreItem>()
    var finish = ArrayList<GradeCheckBean.ScoreItem>()

    override fun onResume() {
        super.onResume()
        var type111 = 0
        if (type == Extras.TYPE_JIXIAO) {
            type111 = 1
        } else if (type == Extras.TYPE_TIAOZHENG) {
            type111 = 3
        }
        SoguApi.getService(application)
                .check(type111)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            unfinish = ready_grade!!
                            finish = finish_grade!!
                            loadData()
                        }
                        //judgeFinish()
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

    fun judgeFinish() {
        if (unfinish.size != 0) {
            return
        }
        toolbar_menu.text = "查看分数"
        toolbar_menu.setOnClickListener {
            ScoreListActivity.start(context)
        }
    }
}
