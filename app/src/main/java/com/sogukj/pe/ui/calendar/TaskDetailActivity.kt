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
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
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

class TaskDetailActivity : ToolbarActivity(), CommentListener {
    lateinit var adapter: RecyclerAdapter<TaskDetailBean.Record>
    lateinit var window: CommentWindow
    lateinit var taskItemBean: TaskItemBean.ItemBean

    companion object {
        fun start(ctx: Activity?, bean: TaskItemBean.ItemBean) {
            val intent = Intent(ctx, TaskDetailActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    override val menuId: Int
        get() = R.menu.task_modify

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        taskItemBean = intent.getSerializableExtra(Extras.DATA) as TaskItemBean.ItemBean
        title = "任务详情"
        setBack(true)



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

        doRequest(taskItemBean.data_id)
        taskTitle.text = taskItemBean.title
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
//                            if (user?.uid != it.info?.pub_id) {
//                                mMenu.getItem(0).isVisible = false
//                                mMenu.getItem(0).isEnabled = false
//                            } else {
//                                mMenu.getItem(0).isVisible = true
//                                mMenu.getItem(0).isEnabled = true
//                            }
                            it.record?.let {
                                adapter.dataList.addAll(it)
                            }
                            it.info?.let {
                                taskNumber.text = TextStrSplice("任务编号: ${it.number}", 5)
                                arrangeTime.text = TextStrSplice("安排时间: ${it.timing}", 5)
                                taskPublisher.text = TextStrSplice("任务发布者: ${it.publisher}", 6)
                                taskExecutive.text = TextStrSplice("任务执行者: ${it.executor}", 6)
                                taskCcPerson.text = TextStrSplice("抄送人: ${it.watcher}", 4)
                                taskDetail.text = TextStrSplice("任务详情: ${it.info}", 5)
                            }
                        }
                    } else {
                        showToast(payload.message)
                    }
                })

    }

    private fun TextStrSplice(text: String, start: Int): SpannableString {
        val spStr = SpannableString(text)
        spStr.setSpan(ForegroundColorSpan(Color.parseColor("#808080")), start, text.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        return spStr
    }

    override fun confirmListener(comment: String) {
        SoguApi.getService(application)
                .addComment(taskItemBean.data_id, comment)
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.task_modify -> {
                ModifyTaskActivity.start(this,taskItemBean.data_id)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
