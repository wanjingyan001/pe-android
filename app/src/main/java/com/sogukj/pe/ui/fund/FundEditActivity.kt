package com.sogukj.pe.ui.fund

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.netease.nim.uikit.common.ui.imageview.CircleImageView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundDetail
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_edit.*
import org.jetbrains.anko.find

class FundEditActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<FundDetail.NameList>

    override val menuId: Int
        get() = R.menu.menu_mark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val flag = super.onCreateOptionsMenu(menu)
        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
        menuMark?.title = "保存"
        return flag
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mark -> {
                setResult(Activity.RESULT_OK)
                //基金保存接口
                finish()
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_edit)
        val data = intent.getSerializableExtra(Extras.DATA) as FundSmallBean
        setBack(true)
        title = data.fundName
        run {
            adapter = RecyclerAdapter(this, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_fund_detail_name_list, parent)
                object : RecyclerHolder<FundDetail.NameList>(convertView) {
                    val headImg = convertView.find<CircleImageView>(R.id.commissionHeadImg)
                    val commissionName = convertView.find<TextView>(R.id.commissionName)
                    override fun setData(view: View, data: FundDetail.NameList, position: Int) {
                        commissionName.text = data.name
                        if (data.url.isNullOrEmpty()) {
                            headImg.setImageResource(R.drawable.nim_avatar_default)
                        } else {
                            Glide.with(this@FundEditActivity)
                                    .load(data.url)
                                    .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                                    .into(headImg)
                        }
                    }
                }
            })
            val manager = LinearLayoutManager(this)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            commissionList.layoutManager = manager
            commissionList.adapter = adapter
        }

        getFundDetail(data.id)
    }


    private fun getFundDetail(fundId: Int) {
        SoguApi.getService(application)
                .getFundDetail(fundId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            Log.d(FundDetailActivity.TAG, Gson().toJson(this))
                            find<EditText>(R.id.administrator).setText(administrator)
                            find<EditText>(R.id.regTime).setText(regTime)
                            find<EditText>(R.id.contributeSize).setText(contributeSize)
                            find<EditText>(R.id.actualSize).setText(actualSize)
                            find<EditText>(R.id.duration).setText(duration)
                            find<EditText>(R.id.partners).setText(partners)
                            find<EditText>(R.id.mode).setText(mode)
                            find<EditText>(R.id.commission).setText(commission)
                            find<EditText>(R.id.manageFees).setText(manageFees)
                            find<EditText>(R.id.carry).setText(carry)

                            if(administrator.isNullOrEmpty()){
                                find<EditText>(R.id.administrator).setSelection(0)
                            } else {
                                find<EditText>(R.id.administrator).setSelection(administrator!!.length)
                            }
//                            find<EditText>(R.id.regTime).setSelection(regTime.length)
//                            find<EditText>(R.id.contributeSize).setSelection(contributeSize.length)
//                            find<EditText>(R.id.actualSize).setSelection(actualSize.length)
//                            find<EditText>(R.id.duration).setSelection(duration.length)
//                            find<EditText>(R.id.partners).setSelection(partners.length)
//                            find<EditText>(R.id.mode).setSelection(mode.length)
//                            find<EditText>(R.id.commission).setSelection(commission.length)
//                            find<EditText>(R.id.manageFees).setSelection(manageFees.length)
//                            find<EditText>(R.id.carry).setSelection(carry.length)
                            list?.let {
                                adapter.dataList.addAll(it)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                })
    }
}
