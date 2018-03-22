package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.util.CharacterParser
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_dst_city.*
import java.lang.reflect.Field
import kotlin.collections.ArrayList

class DstCityActivity : ToolbarActivity() {

    lateinit var inflater: LayoutInflater

    companion object {
        val TAG = DstCityActivity::class.java.simpleName
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, DstCityActivity::class.java)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dst_city)
        setBack(true)
        title = "目的城市"

        inflater = LayoutInflater.from(context)

        initView()
    }

    //实例化汉字转拼音类
    private var characterParser = CharacterParser.getInstance()

    private var loadFinish = false

    private fun initView() {
        side_bar.setTextView(dialog)
        //设置右侧触摸监听
        side_bar.setOnTouchingLetterChangedListener(object : SideBar.OnTouchingLetterChangedListener {
            override fun onTouchingLetterChanged(s: String?) {
                if (loadFinish == false) {
                    return
                }
                //该字母首次出现的位置
                val position = getPositionForSection(s!!.get(0).toInt())
                Log.e("position", "${position}")
                Log.e("char", s.toString())
                if (position != -1) {
                    var layout = mRootScrollView.findViewWithTag("group${position}") as LinearLayout

                    mRootScrollView.post(Runnable {
                        val location = IntArray(2)
                        layout.getLocationInWindow(location)
                        var offset = location[1] - getStatusBarHeight() + lastOffSet
                        lastOffSet = offset
                        mRootScrollView.smoothScrollTo(0, offset)
                    })
                }
            }
        })

        chosenAdapter = ChosenAdapter(context)
        chosenGrid.adapter = chosenAdapter
        chosenGrid.setOnItemClickListener { parent, view, position, id ->
            var city = chosenAdapter.data.get(position)
            updateList(city, false)
            chosenAdapter.data.remove(city)
            chosenAdapter.notifyDataSetChanged()
        }

        historyAdapter = HistoryAdapter(context)
        historyGrid.adapter = historyAdapter
        historyGrid.setOnItemClickListener { parent, view, position, id ->
            var city = historyAdapter.data.get(position)
            if (chosenAdapter.data.contains(city)) {
                //已点过
                updateList(city, false)
                chosenAdapter.data.remove(city)
                chosenAdapter.notifyDataSetChanged()
            } else {
                //未点过
                updateList(city, true)
                chosenAdapter.data.add(city)
                chosenAdapter.notifyDataSetChanged()
            }
        }

        doRequest()
    }

    private var lastOffSet = 0

    //顶部状态栏（电量）+标题
    private fun getStatusBarHeight(): Int {
        var c: Class<*>? = null
        var obj: Any? = null
        var field: Field? = null
        var x = 0
        var sbar = 0
        try {
            c = Class.forName("com.android.internal.R\$dimen")
            obj = c!!.newInstance()
            field = c.getField("status_bar_height")
            x = Integer.parseInt(field!!.get(obj).toString())
            sbar = resources.getDimensionPixelSize(x)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return sbar + Utils.dpToPx(context, 56)
    }

    private var groups = ArrayList<String>()
    private var childs = ArrayList<ArrayList<CityArea.City>>()
    private var adapters = ArrayList<CityAdapter>()

    fun doRequest() {
        showToast("获取城市列表，请等待")
        SoguApi.getService(application)
                .getCityArea()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        filledData(payload.payload!!)

                        side_bar.setB(groups)

                        addToHistory(childs.get(0))

                        initAdapter()
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    fun initAdapter() {
        loadFinish = false
        for (groupIndex in 0 until groups.size) {
            var Groupview = inflater.inflate(R.layout.item, null)
            var tvLetter = Groupview.findViewById(R.id.catalog) as TextView
            tvLetter.text = groups[groupIndex]
            Groupview.tag = "group${groupIndex}"
            cityLayout.addView(Groupview)

            var Childview = inflater.inflate(R.layout.child_item_dstcity, null)
            cityLayout.addView(Childview)
            Childview.tag = "child${groupIndex}"
            var mCityLv = Childview.findViewById(R.id.city) as MyListView
            var adapter = CityAdapter(context, childs.get(groupIndex))
            adapters.add(adapter)
            mCityLv?.adapter = adapter
            mCityLv?.setOnItemClickListener { parent, view, position, id ->
                var city = childs.get(groupIndex)[position]
                city.seclected = !city.seclected
                addToCurrent(city)
                adapter.notifyDataSetChanged()
            }
        }
        loadFinish = true
    }

    lateinit var chosenAdapter: ChosenAdapter

    fun addToCurrent(city: CityArea.City) {
        if (city.seclected == false) {//点两次
            chosenAdapter.data.remove(city)
            chosenAdapter.notifyDataSetChanged()
            return
        }
        if (chosenAdapter.data.size == 6) {
            showToast("目的城市数目不能超过6个")
            //添加不了，注意selected需要更新
            updateList(city, false)
            return
        }
        chosenAdapter.data.add(city)
        chosenAdapter.notifyDataSetChanged()
    }

    fun updateList(city: CityArea.City, newState: Boolean) {
        for (fatherIndex in 0 until childs.size) {
            var child = childs[fatherIndex]
            for (childIndex in 0 until child.size) {
                if (child[childIndex].id == city.id) {
                    childs[fatherIndex][childIndex].seclected = newState
                    adapters[fatherIndex].notifyDataSetChanged()
                    break
                }
            }
        }
    }

    lateinit var historyAdapter: HistoryAdapter

    fun addToHistory(citys: ArrayList<CityArea.City>) {
        historyAdapter.data.addAll(citys.subList(0, 5))
        historyAdapter.notifyDataSetChanged()
    }

    /**
     * 为ListView填充数据
     */
    private fun filledData(list: List<CityArea>) {
        //java 用for循环为一个字符串数组输入从a到z的值。//groups
        var result = ""
        var i = 0
        var j = 'a'
        while (i < 26) {
            groups.add((j.toString() + "").toUpperCase())
            result += j.toString() + " "//连起来，空格隔开
            i++
            j++
        }

        //获取所有城市列表
        var cities = ArrayList<CityArea.City>()
        for (provIndex in list.indices) {
            var province = list[provIndex]
            for (cityIndex in province.city!!.indices) {
                var city = province.city!![cityIndex]

                //汉字转换成拼音
                val sortString = characterParser.getAlpha(city.name).toUpperCase().substring(0, 1)
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]".toRegex())) {
                    city.sortLetters = sortString.toUpperCase()
                } else {
                    city.sortLetters = "#"
                }

                cities.add(city)
            }
        }

        //组装格式
        for (index in groups.indices) {
            var innerList = ArrayList<CityArea.City>()
            for (cityIndex in cities.indices) {
                if (cities[cityIndex].sortLetters.equals(groups[index])) {
                    innerList.add(cities[cityIndex])
                }
            }
            childs.add(innerList)
        }
        for (i in (childs.size - 1) downTo 0) {
            if (childs[i].size == 0) {
                childs.removeAt(i)
                groups.removeAt(i)
            }
        }
    }

    /**
     * 根据ListView的当前位置获取匪类的首字母的Char ascii值
     *
     * @param position
     * @return
     */
    fun getSectionForPosition(position: Int): Int {
        return groups.get(position).toInt()
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     *
     * @param section
     * @return
     */
    fun getPositionForSection(section: Int): Int {
        for (i in 0 until groups.size) {
            val sortStr = groups.get(i)
            val firstChar = sortStr.toUpperCase().get(0)
            if (firstChar.toInt() == section) {
                return i
            }
        }
        return -1
    }
}
