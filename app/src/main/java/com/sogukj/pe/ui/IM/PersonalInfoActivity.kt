package com.sogukj.pe.ui.IM

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_personal_info.*
import org.jetbrains.anko.toast

class PersonalInfoActivity : AppCompatActivity(), View.OnClickListener {
    var user: UserBean? = null
    val CALL_PHONE_PERMISSION = arrayOf(Manifest.permission.CALL_PHONE)

    companion object {
        fun start(ctx: Context, user: UserBean) {
            val intent = Intent(ctx, PersonalInfoActivity::class.java)
            intent.putExtra(Extras.DATA, user)
            ctx.startActivity(intent)
        }

        fun start(ctx: Context?, uid: Int) {
            val intent = Intent(ctx, PersonalInfoActivity::class.java)
            intent.putExtra(Extras.ID, uid)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        team_toolbar.setNavigationIcon(R.drawable.sogu_ic_back)
        team_toolbar.setNavigationOnClickListener { finish() }
        user = intent.getSerializableExtra(Extras.DATA) as UserBean?
        val bean = Store.store.getUser(this)
        if (user != null) {
            user?.let {
                if (it.accid == bean!!.accid) {
                    communicationLayout.visibility = View.INVISIBLE
                }
                name.text = it.name
                position.text = it.position
                company.text = ""
                remarks_tv.text = ""
                name_tv.text = it.name
                phone_tv.text = it.phone
                email_tv.text = it.email
                department_tv.text = it.depart_name
                position_tv.text = it.position
            }
        } else {
            val uid = intent.getIntExtra(Extras.ID, -1)
            queryUserInfo(uid)
        }
        sendMsg.setOnClickListener(this)
        call_phone.setOnClickListener(this)
    }

    private fun queryUserInfo(uid: Int) {
        SoguApi.getService(application)
                .userInfo(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val user = payload.payload
                        Log.d("WJY", Gson().toJson(user))
                        val bean = Store.store.getUser(this)
                        user?.let {
                            if (it.accid == bean!!.accid) {
                                communicationLayout.visibility = View.INVISIBLE
                            }
                            name.text = it.name
                            position.text = it.position
                            name_tv.text = it.name
                            phone_tv.text = it.phone
                            company.text = ""
                            remarks_tv.text = ""
                            email_tv.text = it.email
                            department_tv.text = it.depart_name
                            position_tv.text = it.position
                        }
                    } else toast(payload.message!!)
                }, { e ->
                    Trace.e(e)
                })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sendMsg -> {
                if (NimUIKit.getAccount().isNotEmpty()) {
                    NimUIKit.startP2PSession(this, user?.accid)
                }
            }
            R.id.call_phone -> {
                val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, CALL_PHONE_PERMISSION, 1)
                } else {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${user?.phone}"))
                    startActivity(intent)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${user?.phone}"))
                startActivity(intent)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
