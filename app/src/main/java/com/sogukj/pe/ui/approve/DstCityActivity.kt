package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.util.CharacterParser
import com.sogukj.pe.util.PinyinComparator
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_dst_city.*
import java.util.*
import kotlin.collections.ArrayList

class DstCityActivity : ToolbarActivity() {

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

        initView()
    }

    //实例化汉字转拼音类
    private var characterParser = CharacterParser.getInstance()

    private fun initView() {
        side_bar.setTextView(dialog)

        //设置右侧触摸监听
        side_bar.setOnTouchingLetterChangedListener(object : SideBar.OnTouchingLetterChangedListener {
            override fun onTouchingLetterChanged(s: String?) {
                //该字母首次出现的位置
                val position = mCityAdapter.getPositionForSection(s!!.get(0).toInt())
                if (position != -1) {
                    listview_pro.setSelection(position)
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

    private var groups = ArrayList<String>()
    private var childs = ArrayList<ArrayList<CityArea.City>>()

    fun doRequest() {
        SoguApi.getService(application)
                .getCityArea()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        filledData(payload.payload!!)

                        addToHistory(childs.get(0))
                        mCityAdapter = MyExpAdapter(context, groups, childs, object : MyExpAdapter.onChildClick {
                            override fun onClick(city: CityArea.City) {
                                //第一次点击添加
                                //第二次点击删除
                                //添加不了，注意selected需要更新
                                addToCurrent(city)
                            }
                        })
                        listview_pro.setAdapter(mCityAdapter)
                        for (i in 0 until groups.size) {
                            listview_pro.expandGroup(i)
                        }
                        listview_pro.setOnGroupClickListener(object : ExpandableListView.OnGroupClickListener {
                            override fun onGroupClick(parent: ExpandableListView?, v: View?, groupPosition: Int, id: Long): Boolean {
                                return true
                            }
                        })
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    lateinit var mCityAdapter: MyExpAdapter

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
                    break
                }
            }
        }
        mCityAdapter.notifyDataSetChanged()
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

    class MyExpAdapter(val context: Context, val group: ArrayList<String>,
                       val childs: ArrayList<ArrayList<CityArea.City>>, val listener: onChildClick) : BaseExpandableListAdapter() {

        override fun getGroup(groupPosition: Int): Any {
            return group[groupPosition]
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: GroupHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item, null)
                holder = GroupHolder()
                holder.tvLetter = view.findViewById(R.id.catalog) as TextView
                view.setTag(holder)
            } else {
                holder = view.getTag() as GroupHolder
            }

            holder.tvLetter!!.setVisibility(View.VISIBLE)
            holder.tvLetter!!.setText(group.get(groupPosition))

            return view!!
        }

        class GroupHolder {
            var tvLetter: TextView? = null
            var tvTitle: TextView? = null
            var mRed: ImageView? = null
            var mLayout: LinearLayout? = null
            var mIv: ImageView? = null

            var mCityLv: MyListView? = null
        }

        /**
         * 根据ListView的当前位置获取匪类的首字母的Char ascii值
         *
         * @param position
         * @return
         */
        fun getSectionForPosition(position: Int): Int {
            return group.get(position).toInt()
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         *
         * @param section
         * @return
         */
        fun getPositionForSection(section: Int): Int {
            for (i in 0 until group.size) {
                val sortStr = group.get(i)
                val firstChar = sortStr.toUpperCase().get(0)
                if (firstChar.toInt() == section) {
                    return i
                }
            }
            return -1
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            //return childs[groupPosition].size
            return 1
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return childs[groupPosition][childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: GroupHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.child_item_dstcity, null)
                holder = GroupHolder()
                holder.mCityLv = view.findViewById(R.id.city) as MyListView
                view.setTag(holder)
            } else {
                holder = view.getTag() as GroupHolder
            }
            var adapter = CityAdapter(context, childs.get(groupPosition))
            holder.mCityLv?.adapter = adapter
            holder.mCityLv?.setOnItemClickListener { parent, view, position, id ->
                var city = childs.get(groupPosition)[position]
                city.seclected = !city.seclected
                listener.onClick(city)
                notifyDataSetChanged()
            }
            return view!!
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return group.size
        }

        interface onChildClick {
            fun onClick(city: CityArea.City)
        }
    }
}
