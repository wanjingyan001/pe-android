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
import com.sogukj.pe.view.CityAdapter
import com.sogukj.pe.view.MyListView
import com.sogukj.pe.view.ProvinceAdapter
import com.sogukj.pe.view.SideBar
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

    lateinit var adapter: ProvinceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dst_city)
        setBack(true)
        title = "目的城市"

        initView()
    }

    //实例化汉字转拼音类
    private var characterParser = CharacterParser.getInstance()
    private var pinyinComparator = PinyinComparator()

    private fun initView() {
        side_bar.setTextView(dialog)

        //设置右侧触摸监听
        side_bar.setOnTouchingLetterChangedListener(object : SideBar.OnTouchingLetterChangedListener {
            override fun onTouchingLetterChanged(s: String?) {
                //该字母首次出现的位置
                val position = adapter.getPositionForSection(s!!.get(0).toInt())
                if (position != -1) {
                    listview_pro.setSelection(position)
                }
            }
        })

        doRequest()
    }

    private var groups = ArrayList<CityArea>()
    private var childs = ArrayList<ArrayList<CityArea.City>>()

    fun doRequest() {
        SoguApi.getService(application)
                .getCityArea()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        groups = filledData(payload.payload!!)

                        //根据a-z进行排序源数据
                        Collections.sort(groups, pinyinComparator)

                        //初始化适配器
                        adapter = ProvinceAdapter(context, groups)
                        //绑定适配器
                        //listview_pro.setAdapter(adapter)
                        childs.clear()
                        for (list in groups) {
                            childs.add(list.city!!)
                        }
                        listview_pro.setAdapter(MyExpAdapter(context, groups, childs, object : MyExpAdapter.onChildClick {
                            override fun onClick(city: CityArea.City) {
                                addToCurrent(city)
                            }
                        }))
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    fun addToCurrent(city: CityArea.City) {

    }

    fun addToHistory() {

    }

    /**
     * 为ListView填充数据
     */
    private fun filledData(list: List<CityArea>): ArrayList<CityArea> {
        val mSortList = ArrayList<CityArea>()

        for (i in list.indices) {
            val province = CityArea()
            province.name = list[i].name
            province.id = list[i].id
            province.city = list[i].city
            //汉字转换成拼音
            val pinyin = characterParser.getSelling(list[i].name)
            val sortString = pinyin.substring(0, 1).toUpperCase()//获取拼音首字母
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]".toRegex())) {
                province.sortLetters = sortString.toUpperCase()
            } else {
                province.sortLetters = "#"
            }

            mSortList.add(province)
        }
        return mSortList

    }

    class MyExpAdapter(val context: Context, val group: ArrayList<CityArea>,
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
            val province = group.get(groupPosition)
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item, null)
                holder = GroupHolder()
                holder.tvLetter = view.findViewById(R.id.catalog) as TextView
                holder.tvTitle = view.findViewById(R.id.province) as TextView

                holder.mLayout = view.findViewById(R.id.display) as LinearLayout
                holder.mIv = view.findViewById(R.id.direct) as ImageView
                view.setTag(holder)
            } else {
                holder = view.getTag() as GroupHolder
            }

            //根据position获取分类的首字母的char ascii值
            val section = getSectionForPosition(groupPosition)

            //如果当前位置等于该分类首字母的Char的位置，则认为是第一次出现
            if (groupPosition == getPositionForSection(section)) {
                holder.tvLetter!!.setVisibility(View.VISIBLE)
                holder.tvLetter!!.setText(province.sortLetters)
            } else {
                holder.tvLetter!!.setVisibility(View.GONE)
            }

            holder.tvTitle!!.setText(province.name)

            if (isExpanded) {
                holder.mIv!!.setBackgroundResource(R.drawable.up)
            } else {
                holder.mIv!!.setBackgroundResource(R.drawable.down)
            }

            return view!!
        }

        class GroupHolder {
            var tvLetter: TextView? = null
            var tvTitle: TextView? = null
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
            return group.get(position).sortLetters.get(0).toInt()
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         *
         * @param section
         * @return
         */
        fun getPositionForSection(section: Int): Int {
            for (i in 0 until group.size) {
                val sortStr = group.get(i).sortLetters
                val firstChar = sortStr.toUpperCase().get(0)
                if (firstChar.toInt() == section) {
                    return i
                }
            }
            return -1
        }

        /**
         * 提取英文的首字母，非英文字母用#代替
         *
         * @param str
         * @return
         */
        private fun getAlpha(str: String): String {
            val sortStr = str.trim { it <= ' ' }.substring(0, 1).toUpperCase()
            //正则表达式，判断首字母是否是英文字母
            return if (sortStr.matches("[A-Z]".toRegex())) {
                sortStr
            } else {
                "#"
            }
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
                var city = adapter.data[position]
                city.seclected = !city.seclected
                adapter.notifyDataSetChanged()
                listener.onClick(city)
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
