package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.bean.WeeklyArrangeBean
import com.sogukj.pe.bean.WeeklyReqBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.WorkArrangePerson
import com.sogukj.service.SoguApi
import com.sougukj.isNullOrEmpty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_arrange_edit.*
import okhttp3.internal.Util
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class ArrangeEditActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.arrange_edit_save
    private lateinit var editAdapter: RecyclerAdapter<WeeklyArrangeBean>
    val attendRequestCode = 0x001
    val participateRequestCode = 0x002
    lateinit var data: ArrayList<WeeklyArrangeBean>

    companion object {
        fun start(context: Activity, weeklyData: ArrayList<WeeklyArrangeBean>, offset: String?) {
            val intent = Intent(context, ArrangeEditActivity::class.java)
            intent.putExtra(Extras.LIST, weeklyData)
            intent.putExtra(Extras.ID, offset)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrange_edit)
        title = "班子工作安排详情"
        setBack(true)
        data = intent.getSerializableExtra(Extras.LIST) as ArrayList<WeeklyArrangeBean>
        info(Gson().toJson(data))
        val offset = intent.getStringExtra(Extras.ID)
        editAdapter = RecyclerAdapter(this, { adapter, parent, type ->
            EditHolder(adapter.getView(R.layout.item_arrange_edit, parent))
        })
        editAdapter.dataList.addAll(data)
        arrangeEditList.setItemViewCacheSize(0)
        arrangeEditList.layoutManager = LinearLayoutManager(this)
        arrangeEditList.adapter = editAdapter


        if (offset != null) {
            val inflate = layoutInflater.inflate(R.layout.layout_arrange_weekly_header, arrangeEditList, false)
            arrangeEditList.addHeaderView(inflate)
            when (offset.toInt()) {
                -1 -> {
                    inflate.backgroundResource = R.drawable.bg_last_week
                    inflate.find<TextView>(R.id.weeklyTv).text = "上周"
                }
                0 -> {
                    inflate.backgroundResource = R.drawable.bg_this_week
                    inflate.find<TextView>(R.id.weeklyTv).text = "本周"
                }
                1 -> {
                    inflate.backgroundResource = R.drawable.bg_next_week
                    inflate.find<TextView>(R.id.weeklyTv).text = "下周"
                }
                else -> {
                    inflate.backgroundColor = Color.parseColor("#f7f9fc")
                    val firstTime = data[0].date
                    val lastTime = data[6].date
                    inflate.find<TextView>(R.id.weeklyTv).text = "${firstTime?.substring(5, firstTime.length)}~${lastTime?.substring(5, lastTime.length)}"

                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_edit -> {
                submitWeeklyWork()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Extras.RESULTCODE) {
            val list = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            val persons = ArrayList<WeeklyArrangeBean.Person>()
            list.forEach {
                val person = WeeklyArrangeBean().Person()
                person.name = it.name
                person.user_id = it.uid
                person.url = it.url
                person.position = it.position
                persons.add(person)
            }
            val position = data.getIntExtra(Extras.ID, 0)
            when (requestCode) {
                attendRequestCode -> {
                    runOnUiThread {
                        this.data[position].attendee = persons
                        arrangeEditList.adapter = editAdapter
                    }
                }
                participateRequestCode -> {
                    runOnUiThread {
                        this.data[position].participant = persons
                        arrangeEditList.adapter = editAdapter
                    }
                }
            }
        }
    }

    private fun submitWeeklyWork() {
        val newList = data.filter { !it.reasons.isNullOrEmpty() && it.reasons != "" }
        val findBean = data.find { (!it.place.isNullOrEmpty() || !it.attendee.isNullOrEmpty() || !it.participant.isNullOrEmpty()) && it.reasons.isNullOrEmpty() }
        findBean?.let {
            toast("请填写工作内容")
            return
        }
        if (newList.isEmpty()) {
            toast("请填写工作内容")
            return
        }
        SoguApi.getService(application)
                .submitWeeklyWork(WeeklyReqBean(newList))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        toast("保存成功")
                        val intent = Intent()
                        intent.putExtra(Extras.DATA, data)
                        setResult(Extras.RESULTCODE, intent)
                        finish()
                    } else {
                        toast(payload.message.toString())
                    }
                }, { e ->
                    Trace.e(e)
                    hideProgress()
                }, {
                    hideProgress()
                }, {
                    showProgress("正在保存")
                })
    }

    inner class EditHolder(convertView: View) : RecyclerHolder<WeeklyArrangeBean>(convertView) {
        val weeklyTv = convertView.find<TextView>(R.id.weeklyTv)
        val dayOfMonth = convertView.find<TextView>(R.id.dayOfMonth)
        val workContentEdit = convertView.find<EditText>(R.id.workContentEdit)
        val attendLayout = convertView.find<LinearLayout>(R.id.attendLayout)
        val personAttend = convertView.find<WorkArrangePerson>(R.id.person_attend)
        val participateLayout = convertView.find<LinearLayout>(R.id.participateLayout)
        val personParticipate = convertView.find<WorkArrangePerson>(R.id.person_participate)
        val addressLayout = convertView.find<LinearLayout>(R.id.addressLayout)
        val addressEdit = convertView.find<EditText>(R.id.addressEdit)

        override fun setData(view: View, data: WeeklyArrangeBean, position: Int) {
            weeklyTv.text = data.weekday
            dayOfMonth.text = data.date?.substring(5, data.date?.length!!)
            workContentEdit.filters = Utils.getFilter(ctx)
            addressEdit.filters = Utils.getFilter(ctx)
            if (data.reasons.isNullOrEmpty()) {
                workContentEdit.setText("")
            }
            data.reasons?.let {
                workContentEdit.setText(it.trim())
//                if (it.isNotEmpty())
//                    workContentEdit.setSelection(it.length)
            }
            val attList = ArrayList<UserBean>()
            data.attendee?.let {
                it.forEach {
                    val bean = UserBean()
                    bean.uid = it.user_id
                    bean.url = it.url
                    bean.name = it.name!!
                    it.position?.let {
                        bean.position = it
                    }
                    attList.add(bean)
                }
                if (attList.isNotEmpty()) {
                    personAttend.setPersons(attList)
                } else {
                    personAttend.setPersons(ArrayList())
                }
            }
            val particList = ArrayList<UserBean>()
            data.participant?.let {
                it.forEach {
                    val bean = UserBean()
                    bean.uid = it.user_id
                    bean.url = it.url
                    bean.name = it.name!!
                    it.position?.let {
                        bean.position = it
                    }
                    particList.add(bean)
                }
                if (particList.isNotEmpty()) {
                    personParticipate.setPersons(particList)
                } else {
                    personParticipate.setPersons(ArrayList())
                }
            }
            data.place?.let {
                addressEdit.setText(it.trim())
            }
            attendLayout.setOnClickListener {
                ArrangePersonActivity.start(this@ArrangeEditActivity, attList, attendRequestCode, position)
            }
            participateLayout.setOnClickListener {
                ArrangePersonActivity.start(this@ArrangeEditActivity, particList, participateRequestCode, position)
            }
            val contentWatcher: TextWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val content = workContentEdit.text.toString().trim()
                    this@ArrangeEditActivity.data[position].reasons = content
                }

            }
            workContentEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    workContentEdit.setSelection(workContentEdit.text.length)
                    workContentEdit.addTextChangedListener(contentWatcher)
                } else {
                    workContentEdit.removeTextChangedListener(contentWatcher)
                }
            }
            addressLayout.setOnClickListener {
                addressEdit.requestFocus()
            }
            val addressWatcher: TextWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    this@ArrangeEditActivity.data[position].place = addressEdit.text.toString().trim()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
            }
            addressEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    addressEdit.setSelection(addressEdit.text.toString().length)
                    addressEdit.addTextChangedListener(addressWatcher)
                } else {
                    addressEdit.removeTextChangedListener(addressWatcher)
                }
            }
        }
    }
}
