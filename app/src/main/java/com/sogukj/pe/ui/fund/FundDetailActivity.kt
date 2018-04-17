package com.sogukj.pe.ui.fund

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundDetail
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_detail.*
import org.jetbrains.anko.find

class FundDetailActivity : ToolbarActivity() {
    lateinit var adapter: RecyclerAdapter<UserBean>

    companion object {
        val TAG = FundDetailActivity::class.java.simpleName

        fun start(ctx: Context?, data: FundSmallBean) {
            val intent = Intent(ctx, FundDetailActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    override val menuId: Int
        get() = R.menu.menu_mark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val flag = super.onCreateOptionsMenu(menu)
        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
        menuMark?.title = "编辑"
        return flag
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mark -> {
                val intent = Intent(context, FundEditActivity::class.java)
                intent.putExtra(Extras.DATA, data)
                startActivityForResult(intent, 0x001)
            }
        }
        return false
    }

    lateinit var data: FundSmallBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_detail)
        data = intent.getSerializableExtra(Extras.DATA) as FundSmallBean
        setBack(true)
        title = data.fundName
        run {
            adapter = RecyclerAdapter(this, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_fund_detail_name_list, parent)
                object : RecyclerHolder<UserBean>(convertView) {
                    val headImg = convertView.find<CircleImageView>(R.id.commissionHeadImg)
                    val commissionName = convertView.find<TextView>(R.id.commissionName)
                    override fun setData(view: View, data: UserBean, position: Int) {
                        commissionName.text = data.name
                        if (data.url.isNullOrEmpty()) {
                            val ch = data.name?.first()
                            headImg.setChar(ch)
                        } else {
                            Glide.with(context)
                                    .load(data.url)
                                    .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                                    .into(headImg)
                        }
//                        if (data.url.isNullOrEmpty()) {
//                            headImg.setImageResource(R.drawable.nim_avatar_default)
//                        } else {
//                            Glide.with(this@FundDetailActivity)
//                                    .load(data.url)
//                                    .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
//                                    .into(headImg)
//                        }
                    }
                }
            })
            val manager = LinearLayoutManager(this)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            commissionList.layoutManager = manager
            commissionList.adapter = adapter
        }

        getFundDetail(data.id)

        run {
            structure.setOnClickListener({ FundStructureActivity.start(this, data) })
            fundsDetail.setOnClickListener({ FundAccountActivity.start(this, data) })
            fundsWenShu.setOnClickListener({ FundBookActivity.start(this, data) })
        }
    }


    private fun getFundDetail(fundId: Int) {
        SoguApi.getService(application)
                .getFundDetail(fundId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            Log.d(TAG, Gson().toJson(this))
                            find<TextView>(R.id.administrator).text = administrator
                            find<TextView>(R.id.regTime).text = regTime
                            find<TextView>(R.id.contributeSize).text = contributeSize
                            find<TextView>(R.id.actualSize).text = actualSize
                            find<TextView>(R.id.duration).text = duration
                            find<TextView>(R.id.partners).text = partners
                            find<TextView>(R.id.mode).text = mode
                            find<TextView>(R.id.commission).text = commission
                            find<TextView>(R.id.manageFees).text = manageFees
                            find<TextView>(R.id.carry).text = carry
                            list?.let {
                                adapter.dataList.clear()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data1: Intent?) {
        super.onActivityResult(requestCode, resultCode, data1)
        if (requestCode == 0x001 && resultCode == Activity.RESULT_OK) {
            getFundDetail(data.id)
        }
    }

}
