package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.CommentWindow
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_task_detail.*
import org.jetbrains.anko.find
import kotlin.properties.Delegates

class TaskDetailActivity : ToolbarActivity(), CommentListener, View.OnClickListener {


    lateinit var adapter: RecyclerAdapter<TaskDetailBean.Record>
    lateinit var window: CommentWindow
    var data_id: Int by Delegates.notNull()
    var titleStr: String = ""


    companion object {
        fun start(ctx: Activity?, data_id: Int, title: String, name: String) {
            val intent = Intent(ctx, TaskDetailActivity::class.java)
            intent.putExtra(Extras.ID, data_id)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
    }

    override val menuId: Int
        get() = R.menu.task_modify

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        data_id = intent.getIntExtra(Extras.ID, -1)
        titleStr = intent.getStringExtra(Extras.TITLE)
        title = "任务详情"
        setBack(true)
        delete.setOnClickListener(this)

        adapter = RecyclerAdapter(this, { _adapter, parent, position ->
            val convertView = _adapter.getView(R.layout.item_comment_list, parent)
            object : RecyclerHolder<TaskDetailBean.Record>(convertView) {
                val headerImage = convertView.find<CircleImageView>(R.id.headerImage)
                val commentName = convertView.find<TextView>(R.id.commentName)
                val info = convertView.find<TextView>(R.id.info)
                val time = convertView.find<TextView>(R.id.time)
                val type = convertView.find<TextView>(R.id.type)
                override fun setData(view: View, data: TaskDetailBean.Record, position: Int) {
                    Glide.with(this@TaskDetailActivity)
                            .load(data.url)
                            .into(headerImage)
                    commentName.text = data.name
                    when (data.type) {
                        "1" -> {
                            info.text = "布置任务"
                            type.visibility = View.GONE
                        }
                        "2" -> {
                            info.text = "接受任务"
                            type.text = TextStrSplice("意见:${data.content}", 3)
                        }
                        "3" -> {
                            info.text = "拒绝任务"
                            type.text = TextStrSplice("原因:${data.content}", 3)
                        }
                        else -> {
                            info.text = "评价任务"
                            type.text = TextStrSplice("评论:${data.content}", 3)
                        }
                    }
                    time.text = data.time
                }
            }
        })
        commentList.layoutManager = LinearLayoutManager(this)
        commentList.adapter = adapter

        window = CommentWindow(this, this)
        commentTv.setOnClickListener {
            window.showAtLocation(find(R.id.task_detail_main), Gravity.BOTTOM, 0, 0)
        }

        doRequest(data_id)
        taskTitle.text = titleStr
    }

    fun doRequest(data_id: Int) {
        if (data_id == 0) {
            return
        }
        SoguApi.getService(application)
                .showTaskDetail(data_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            val user = Store.store.getUser(this)
                            if (user?.uid != it.info?.pub_id) {
                                mMenu.getItem(0).isVisible = false
                                mMenu.getItem(0).isEnabled = false
                                delete.visibility = View.GONE
                            } else {
                                mMenu.getItem(0).isVisible = true
                                mMenu.getItem(0).isEnabled = true
                                delete.visibility = View.VISIBLE
                            }
                            it.record?.let {
                                adapter.dataList.addAll(it)
                            }
                            it.info?.let {
                                taskNumber.text = TextStrSplice("任务编号: ${it.number}", 5)
                                arrangeTime.text = TextStrSplice("安排时间: ${it.timing}", 5)
                                taskPublisher.text = TextStrSplice("任务发布者: ${it.publisher}", 6)
                                related_project.text = TextStrSplice("关联项目: ${it.cName}", 5)
                                taskExecutive.text = TextStrSplice("任务执行者: ${it.executor}", 6)
                                val watchStr = if (it.watcher == null) "" else it.watcher
                                taskCcPerson.text = TextStrSplice("抄送人: $watchStr", 4)
                                taskDetail.text = TextStrSplice("任务详情: ${it.info}", 5)
                            }
                        }
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })

    }

    private fun TextStrSplice(text: String, start: Int): SpannableString {
        val spStr = SpannableString(text)
        spStr.setSpan(ForegroundColorSpan(Color.parseColor("#808080")), start, text.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        return spStr
    }

    override fun confirmListener(comment: String) {
        Utils.closeInput(this, commentTv)
        SoguApi.getService(application)
                .addComment(data_id, comment)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            adapter.dataList.add(it)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        showToast(payload.message)
                    }
                })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.delete -> {
                MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .title("删除")
                        .content("确认删除?")
                        .positiveText("确认")
                        .negativeText("取消")
                        .onPositive { dialog, which ->
                            SoguApi.getService(application)
                                    .deleteTask(data_id)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe({ payload ->
                                        if (payload.isOk) {
                                            showToast("删除成功")
                                            finish()
                                        } else {
                                            showToast(payload.message)
                                        }
                                    })
                        }
                        .show()

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.task_modify -> {
                ModifyTaskActivity.startForModify(this, data_id, intent.getStringExtra(Extras.NAME))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
