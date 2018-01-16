package com.sogukj.pe.ui.project

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_project.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectListFragment : BaseFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_project//To change initializer of created properties use File | Settings | File Templates.
    lateinit var adapter: RecyclerAdapter<ProjectBean>
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments.getInt(Extras.TYPE)
    }

    fun getRecycleView() : RecyclerView {
        return recycler_view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        ll_header.visibility = if (type == TYPE_CB || type == TYPE_DY) View.VISIBLE else View.GONE
//        ll_header2.visibility = if (type == TYPE_LX) View.VISIBLE else View.GONE
//        ll_header1.visibility = if (type == TYPE_GZ || type == TYPE_YT) View.VISIBLE else View.GONE
//        ll_header3.visibility = if (type == TYPE_TC) View.VISIBLE else View.GONE

        ll_header.visibility = View.GONE
        ll_header1.visibility = View.GONE
        ll_header2.visibility = View.GONE
        ll_header3.visibility = View.GONE

        adapter = RecyclerAdapter<ProjectBean>(baseActivity!!, { _adapter, parent, t ->
            when (type) {
                TYPE_GZ, TYPE_YT, TYPE_TC -> ProjectHolder(_adapter.getView(R.layout.item_main_project, parent))
                TYPE_LX -> ProjectHolder(_adapter.getView(R.layout.item_main_project_2, parent))
                TYPE_CB, TYPE_DY -> StoreProjectHolder(_adapter.getView(R.layout.item_main_project_3, parent))
                else -> throw IllegalArgumentException()
            }

        })
        adapter.onItemClick = { v, p ->
            val project = adapter.getItem(p);
//            if (type == TYPE_CB || type == TYPE_DY)
//                StoreProjectAddActivity.startView(baseActivity, project)
//            else
//                ProjectActivity.start(baseActivity, project)

            //ProjectActivity.start(baseActivity, project)
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.CODE, p)
            startActivityForResult(intent, 0x001)
        }

        val user = Store.store.getUser(baseActivity!!)
        if (type != TYPE_GZ)// && user?.is_admin == 1
            adapter.onItemLongClick = { v, p ->
                if (type == TYPE_DY || type == TYPE_CB) {
                } else {
                    editOptions(v, p)
                }
                true
            }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        //recycler_view.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 25)))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        val header = ProgressLayout(baseActivity)
        header.setColorSchemeColors(ContextCompat.getColor(baseActivity, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(baseActivity)
        footer.setAnimatingColor(ContextCompat.getColor(baseActivity, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                offset = 0
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                offset = adapter.dataList.size
                doRequest()
            }

        })
        refresh.setAutoLoadMore(true)
        ll_order_name_1.setOnClickListener { v ->
            asc *= if (orderBy == 1) -1 else 1;
            orderBy = 1
            doRequest()
        }
        ll_order_time_1.setOnClickListener { v ->
            asc *= if (orderBy == 4) -1 else 1;
            orderBy = 4
            doRequest()
        }

        ll_order_name.setOnClickListener { v ->
            asc *= if (orderBy == 1) -1 else 1;
            orderBy = 1
            doRequest()
        }
        ll_order_time.setOnClickListener { v ->
            asc *= if (orderBy == 3) -1 else 1;
            orderBy = 3
            doRequest()
        }
        ll_order_state.setOnClickListener { v ->
            asc *= if (orderBy == 2) -1 else 1;
            orderBy = 2
            doRequest()
        }
        Glide.with(baseActivity)
                .load(Uri.parse("file:///android_asset/img_loading.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE
        handler.postDelayed({
            doRequest()
        }, 100)
        Log.e("onViewCreated", "${type}")
        isViewCreated = true
    }

    override fun onStart() {
        super.onStart()
        Log.e("onStart", "${type}")
        //doRequest()
    }

    var isViewCreated = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser == true && isViewCreated == true) {
            Log.e("setUserVisibleHint", "${type}")
            doRequest()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewCreated = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {
            data?.let {
                Log.e("onActivityResult", "${type}")
                var project = it.getSerializableExtra(Extras.DATA) as ProjectBean
                var position = it.getIntExtra(Extras.CODE, 0)
                adapter.dataList[position] = project
                adapter.notifyDataSetChanged()
            }
        }
    }

    val fmt = SimpleDateFormat("MM/dd HH:mm")
    var offset = 0
    var asc = -1;
    var orderBy = 4;

    fun doRequest() {
        iv_sort_name_1.visibility = View.GONE
        iv_sort_time_1.visibility = View.GONE
        iv_sort_name.visibility = View.GONE
        iv_sort_time.visibility = View.GONE
        iv_sort_state.visibility = View.GONE
        val imgAsc = if (asc == -1) R.drawable.ic_down else R.drawable.ic_up
        if (type == TYPE_CB || type == TYPE_DY) {
            val view = when (Math.abs(orderBy)) {
                1 -> iv_sort_name_1
                4 -> iv_sort_time_1
                else -> {
                    orderBy = 4
                    iv_sort_time_1
                }
            }
            view.setImageResource(imgAsc)
            view.visibility = View.VISIBLE
        } else {
            val view = when (Math.abs(orderBy)) {
                1 -> iv_sort_name
                2 -> iv_sort_state
                else -> {
                    orderBy = 3
                    iv_sort_time
                }
            }
            view.setImageResource(imgAsc)
            view.visibility = View.VISIBLE
        }


        val sort = orderBy * asc;
        val user = Store.store.getUser(baseActivity!!)
        SoguApi.getService(baseActivity!!.application)
                .listProject(offset = offset, type = type, uid = user!!.uid, sort = sort)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (offset == 0)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showToast(payload.message)
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                    iv_loading?.visibility = View.GONE
                    SupportEmptyView.checkEmpty(this, adapter)
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    val b = adapter.dataList.size >= offset + 20
                    refresh?.setEnableLoadmore(b)
//                    if (!b) {
//                        refresh?.setBottomView(LoadMoreView(baseActivity))
//                    }
                    adapter.notifyDataSetChanged()
                    if (offset == 0)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()
                })
    }

    fun editOptions(view: View, position: Int) {
        val popupView = View.inflate(baseActivity, R.layout.pop_edit_project, null)
        val tvAdd = popupView.findViewById(R.id.tv_add) as TextView
        val tvDel = popupView.findViewById(R.id.tv_del) as TextView
        val pop = PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true)
        tvAdd.setOnClickListener { view1 ->
            doAdd(position)
            pop.dismiss()
        }
        tvDel.setOnClickListener { view12 ->
            //            doDel(position)
            pop.dismiss()
            MaterialDialog.Builder(baseActivity!!)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确定要删除这条数据?")
                    .onPositive { materialDialog, dialogAction ->
                        doDel(position)
                    }
                    .positiveText("确定")
                    .negativeText("取消")
                    .show()
        }
        if (type == TYPE_TC) tvAdd.visibility = View.GONE
        var toast_txt = if (type == TYPE_LX) "添加已投" else if (type == TYPE_YT) "添加到退出" else ""
        tvAdd.text = toast_txt
        pop.isTouchable = true
        pop.isFocusable = true
        pop.isOutsideTouchable = true
        pop.setBackgroundDrawable(BitmapDrawable(resources, null as Bitmap?))
        val location = IntArray(2)
        if (view.id != R.id.project_main_layout3) {
            val view = view.find<View>(R.id.tv1)
            view.getLocationInWindow(location)
            val x = location[0]
            val y = location[1]
            pop.showAtLocation(view, Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, y - view.getMeasuredHeight())
        }
    }

    fun doDel(position: Int) {
        val project = adapter.dataList[position]
        SoguApi.getService(baseActivity!!.application)
                .delProject(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("删除成功")
                        adapter.dataList.removeAt(position)
                        adapter.notifyDataSetChanged()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("删除失败")
                })
    }

    fun doAdd(position: Int) {
        var status = if (type == TYPE_LX) 3 else if (type == TYPE_YT) 4 else 0
        val project = adapter.dataList[position]
        SoguApi.getService(baseActivity!!.application)
                //.editProject(project.company_id!!)
                .changeStatus(project.company_id!!, status)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (type == TYPE_LX) {
                            showToast("添加拟投成功")
                        } else if (type == TYPE_YT) {
                            showToast("已添加到退出")
                        }
                        adapter.dataList.removeAt(position)
                        adapter.notifyDataSetChanged()
                    } else showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    if (type == TYPE_LX) {
                        showToast("添加拟投失败")
                    } else if (type == TYPE_YT) {
                        showToast("添加到退出失败")
                    }
                })

    }

    inner class StoreProjectHolder(convertView: View)
        : RecyclerHolder<ProjectBean>(convertView) {


        val tvTitle: TextView
        val btnAdd: TextView
        val tvDate: TextView
        val tvTime: TextView
        val tvEdit: TextView
        val tvDel: TextView
        val tv4: TextView
        val tv5: TextView
        val tv_pingjia: LinearLayout
        val point: TextView

        init {
            tvTitle = convertView.findViewById(R.id.tv_title) as TextView
            btnAdd = convertView.findViewById(R.id.btn_add) as TextView
            tvDate = convertView.findViewById(R.id.tv_date) as TextView
            tvTime = convertView.findViewById(R.id.tv_time) as TextView
            tvEdit = convertView.findViewById(R.id.tv_edit) as TextView
            tvDel = convertView.findViewById(R.id.tv_del) as TextView
            tv4 = convertView.findViewById(R.id.business) as TextView
            tv5 = convertView.findViewById(R.id.ability) as TextView
            tv_pingjia = convertView.findViewById(R.id.pingjia) as LinearLayout
            point = convertView.findViewById(R.id.point) as TextView
        }

        override fun setData(view: View, data: ProjectBean, position: Int) {
            var label = data.shortName
            if (TextUtils.isEmpty(label))
                label = data.name
            tvTitle.text = Html.fromHtml(label)
            val strTime = data.add_time
            tvTime.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                    tvTime.visibility = View.VISIBLE
                }
                tvDate.text = strs
                        .getOrNull(0)
                tvTime.text = strs
                        .getOrNull(1)
            }

            if (type == TYPE_DY) {
                btnAdd.text = "+储备"
                btnAdd.setOnClickListener {
                    MaterialDialog.Builder(baseActivity!!)
                            .theme(Theme.LIGHT)
                            .content("是否确认储备")
                            .negativeText("取消")
                            .positiveText("确认")
                            .onPositive { dialog, which ->
                                doSetUp(position, data, 1)
                                dialog.dismiss()
                            }.show()
                }
            } else if (type == TYPE_CB) {
                btnAdd.text = "+立项"
                btnAdd.setOnClickListener {
                    MaterialDialog.Builder(baseActivity!!)
                            .theme(Theme.LIGHT)
                            .content("是否确认立项")
                            .negativeText("取消")
                            .positiveText("确认")
                            .onPositive { dialog, which ->
                                doSetUp(position, data, 2)
                                dialog.dismiss()
                            }.show()
                }
            }

            tvEdit.setOnClickListener {
                if (type == TYPE_CB) {
                    StoreProjectAddActivity.startEdit(baseActivity, data)
                } else if (type == TYPE_DY) {
                    ProjectAddActivity.startEdit(baseActivity, data)
                }

            }
            tvDel.setOnClickListener {
                MaterialDialog.Builder(baseActivity!!)
                        .theme(Theme.LIGHT)
                        .content("确认删除该数据")
                        .negativeText("取消")
                        .positiveText("确认")
                        .onPositive { dialog, which ->
                            doDel(position)
                            dialog.dismiss()
                        }.show()
            }
            if (type == TYPE_CB) {
                tv_pingjia.visibility = View.VISIBLE
            } else if (type == TYPE_DY) {
                tv_pingjia.visibility = View.GONE
            }
            //convertView.findViewById(R.id.suffix).visibility = View.VISIBLE
            if (data.is_business == 1) {
                tv4.text = "有商业价值"
            } else if (data.is_business == 2) {
                tv4.text = "无商业价值"
            }

            if (data.is_ability == 1) {
                tv5.text = "创始人靠谱"
            } else if (data.is_ability == 2) {
                tv5.text = "创始人不靠谱"
            }

            if (data.is_business == null && data.is_ability == null) {
                tv_pingjia.visibility = View.GONE
            } else {
                tv_pingjia.visibility = View.VISIBLE
                if (data.is_business == null) {
                    tv4.visibility = View.GONE
                } else {
                    tv4.visibility = View.VISIBLE
                }
                if (data.is_ability == null) {
                    tv5.visibility = View.GONE
                } else {
                    tv5.visibility = View.VISIBLE
                }
            }

            if (data.red != null && data.red != 0) {
                point.visibility = View.VISIBLE
                point.text = data.red.toString()
            } else {
                point.visibility = View.GONE
            }
        }

    }

    fun doSetUp(position: Int, data: ProjectBean, status: Int) {
        SoguApi.getService(baseActivity!!.application)
                //.setUpProject(data.company_id!!)
                .changeStatus(data.company_id!!, status!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("修改成功")
                        adapter.dataList.removeAt(position)
                        adapter.notifyDataSetChanged()
                    } else {
                        showToast("修改失败")
                    }
                }, { e -> Trace.e(e) })
    }

    inner class ProjectHolder(view: View)
        : RecyclerHolder<ProjectBean>(view) {

        val tv1: TextView
        val tv2: TextView
        val tv3: TextView
        val tv4: TextView
        val tv5: TextView
        val pingjia: LinearLayout
        val point: TextView

        init {
            tv1 = view.find(R.id.tv1)
            tv2 = view.find(R.id.tv2)
            tv3 = view.find(R.id.tv3)
            tv4 = view.find(R.id.business)
            tv5 = view.find(R.id.ability)
            pingjia = view.find(R.id.pingjia)
            point = view.find(R.id.point)
        }

        override fun setData(view: View, data: ProjectBean, position: Int) {
            var label = data.shortName
            if (TextUtils.isEmpty(label))
                label = data.name
            tv1.text = Html.fromHtml(label)
            if (type == 1) {
                tv2.text = when (data.status) {
                    2 -> "已完成"
                    else -> "准备中"
                }
            } else if (type == 2 || type == 5) {
                tv2.text = data.state
                tv2.textColor = when (data.state) {
                    "初创" -> Color.parseColor("#9e9e9e")
                    "天使轮" -> Color.parseColor("#e64a19")
                    "A轮" -> Color.parseColor("#f57c00")
                    "B轮" -> Color.parseColor("#ffa000")
                    "C轮" -> Color.parseColor("#fbc02d")
                    "D轮" -> Color.parseColor("#afb42b")
                    "E轮" -> Color.parseColor("#689f38")
                    "PIPE轮" -> Color.parseColor("#388e3c")
                    "新三板" -> Color.parseColor("#00796b")
                    "IPO" -> Color.parseColor("#0097a7")
                    else -> Color.parseColor("#9e9e9e")
                }
                val bg = when (data.state) {
                    "初创" -> R.drawable.bg_border_proj1
                    "天使轮" -> R.drawable.bg_border_proj2
                    "A轮" -> R.drawable.bg_border_proj3
                    "B轮" -> R.drawable.bg_border_proj4
                    "C轮" -> R.drawable.bg_border_proj5
                    "D轮" -> R.drawable.bg_border_proj6
                    "E轮" -> R.drawable.bg_border_proj7
                    "PIPE轮" -> R.drawable.bg_border_proj8
                    "新三板" -> R.drawable.bg_border_proj9
                    "IPO" -> R.drawable.bg_border_proj10
                    else -> R.drawable.bg_border_proj1
                }
                tv2.setBackgroundResource(bg)
            }
            tv3.text = when (type) {
                1 -> data.add_time
                2 -> data.update_time
                else -> data.next_time
            }

            if (tv2.text.isNullOrEmpty()) {
                tv2.text = "--"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    tv2.background = null
                }
            }
            if (tv3.text.isNullOrEmpty()) tv3.text = "--"

            if (data.is_business == 1) {
                tv4.text = "有商业价值"
            } else if (data.is_business == 2) {
                tv4.text = "无商业价值"
            }

            if (data.is_ability == 1) {
                tv5.text = "创始人靠谱"
            } else if (data.is_ability == 2) {
                tv5.text = "创始人不靠谱"
            }

            if (data.is_business == null && data.is_ability == null) {
                pingjia.visibility = View.GONE
            } else {
                pingjia.visibility = View.VISIBLE
                if (data.is_business == null) {
                    tv4.visibility = View.GONE
                } else {
                    tv4.visibility = View.VISIBLE
                }
                if (data.is_ability == null) {
                    tv5.visibility = View.GONE
                } else {
                    tv5.visibility = View.VISIBLE
                }
            }
            if (data.red != null && data.red != 0) {
                point.visibility = View.VISIBLE
                point.text = data.red.toString()
            } else {
                point.visibility = View.GONE
            }
        }

    }

    companion object {
        val TAG = ProjectListFragment::class.java.simpleName
        const val TYPE_CB = 4
        const val TYPE_LX = 1
        const val TYPE_YT = 2
        const val TYPE_GZ = 3
        const val TYPE_DY = 6
        const val TYPE_TC = 5

        fun newInstance(type: Int): ProjectListFragment {
            val fragment = ProjectListFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }
}