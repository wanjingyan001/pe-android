package com.sogukj.pe.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.adapter.RecyclerAdapter
import com.sogukj.pe.adapter.RecyclerHolder
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_project.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectListFragment : BaseFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_project//To change initializer of created properties use File | Settings | File Templates.
    lateinit var adapter: RecyclerAdapter<ProjectBean>
    var index: Int = 0
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index = arguments.getInt(Extras.INDEX)
        type = when (index) {
            0 -> 2;
            1 -> 3;
            3 -> 1;
            else -> 1
        };
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ll_header2.visibility = if (index == 2) View.VISIBLE else View.GONE
        ll_header.visibility = if (index != 2) View.VISIBLE else View.GONE
        adapter = RecyclerAdapter<ProjectBean>(baseActivity!!, { _adapter, parent, type ->
            if (index != 2)
                ProjectHolder(_adapter.getView(R.layout.item_main_project, parent))
            else
                ProjectHolder(_adapter.getView(R.layout.item_main_project_2, parent))
        })
        adapter.onItemClick = { v, p ->
            val project = adapter.getItem(p);
            ProjectActivity.start(baseActivity, project)
        }
        if (index != 1)
            adapter.onItemLongClick = { v, p ->
                editOptions(v, p)
                true
            }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
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
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    override fun onStart() {
        super.onStart()
        doRequest()
    }

    val fmt = SimpleDateFormat("MM/dd HH:mm")
    var offset = 0

    fun doRequest() {
        val user = Store.store.getUser(baseActivity!!)
        if (null != user)
            SoguApi.getService(baseActivity!!.application)
                    .listProject(offset = offset, type = type, uid = user!!.uid)
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
                    }, { e ->
                        Trace.e(e)
                        showToast("暂无可用数据")
                    }, {
                        SupportEmptyView.checkEmpty(this, adapter)
                        refresh?.setEnableLoadmore(adapter.dataList.size >= offset + 20)
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
            doDel(position)
            pop.dismiss()
        }
        if (index == 0) tvAdd.visibility = View.GONE
        pop.isTouchable = true
        pop.isFocusable = true
        pop.isOutsideTouchable = true
        pop.setBackgroundDrawable(BitmapDrawable(resources, null as Bitmap?))
        val location = IntArray(2)
        val view = view.find<View>(R.id.tv1)
        view.getLocationInWindow(location)
        val x = location[0]
        val y = location[1]
        pop.showAtLocation(view, Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, y - view.getMeasuredHeight())
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
        val project = adapter.dataList[position]
        SoguApi.getService(baseActivity!!.application)
                .editProject(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("添加拟投成功")
                        adapter.dataList.removeAt(position)
                        adapter.notifyDataSetChanged()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("添加拟投失败")
                })

    }

    inner class ProjectHolder(view: View)
        : RecyclerHolder<ProjectBean>(view) {

        val tv1: TextView
        val tv2: TextView
        val tv3: TextView

        init {
            tv1 = view.find(R.id.tv1)
            tv2 = view.find(R.id.tv2)
            tv3 = view.find(R.id.tv3)
        }

        override fun setData(view: View, data: ProjectBean, position: Int) {
            tv1.text = data.name
            tv2.text = if (type == 2)
                data.state
            else when (data.status) {
                2 -> "已完成"
                else -> "准备中"
            }
            tv3.text = when (type) {
                1 -> data.add_time
                else -> data.update_time
            }
        }

    }

    companion object {
        val TAG = ProjectListFragment::class.java.simpleName

        fun newInstance(idx: Int): ProjectListFragment {
            val fragment = ProjectListFragment()
            val intent = Bundle()
            intent.putInt(Extras.INDEX, idx)
            fragment.arguments = intent
            return fragment
        }
    }
}