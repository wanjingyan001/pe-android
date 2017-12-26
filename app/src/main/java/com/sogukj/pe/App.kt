package com.sogukj.pe

import android.content.Context
import android.content.Intent
import android.support.multidex.MultiDexApplication
import android.util.Log
import com.framework.base.ActivityHelper
import com.google.gson.Gson
import com.mob.MobSDK
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.ui.calendar.ModifyTaskActivity
import com.sogukj.pe.ui.calendar.TaskDetailActivity
import com.sogukj.pe.ui.news.NewsDetailActivity
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject


/**
 * Created by qinfei on 17/7/18.
 */
class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
//        MobSDK.init(this, "137b5c5ce8f55", "b28db523803b31a66b590150cb96c4fd")
        MobSDK.init(this, "214eaf8217e6c", "c1ddfcaa333020a5a06812bc745d508c")
        val mPushAgent = PushAgent.getInstance(this)
        mPushAgent.setDebugMode(false)
        mPushAgent.displayNotificationNumber = 0
        mPushAgent.register(object : IUmengRegisterCallback {

            override fun onSuccess(deviceToken: String) {
                //注册成功会返回device token
                println("IUmengRegisterCallback:$deviceToken")
                Store.store.setUToken(this@App, deviceToken)
            }

            override fun onFailure(s: String, s1: String) {
                println("IUmengRegisterCallback:$s=>$s1")
            }
        })
        PushAgent.getInstance(this).setNotificationClickHandler({ context, uMessage ->
            try {
                Log.d("WJY", "推送==>${Gson().toJson(uMessage)}")
                uMessage.custom?.apply {
                    Log.d("WJY", "推送内容==>${Gson().toJson(uMessage.custom)}")
                    com.sogukj.pe.util.Trace.i("uPush", this)
                    val json = JSONObject(this)
                    val type = json.getInt("type")
                    val data_id = json.getInt("data_id")
                    //1.负面信息  2.任务  3.日程
                    when (type) {
                        1 -> handle(context, json)
                        2 -> TaskDetailActivity.start(context, data_id, uMessage.title, ModifyTaskActivity.Task)
                        3 -> TaskDetailActivity.start(context, data_id, uMessage.title, ModifyTaskActivity.Schedule)
                        else -> {
                        }
                    }
                }
            } catch (e: Exception) {
                Trace.e(e)
            }
        })
    }

    val GSON = Gson();
    fun handle(context: Context, json: JSONObject) {
        try {
            val dataJson = json.getJSONObject("data")
            val data = GSON.fromJson(dataJson.toString(), NewsBean::class.java)
            val intent: Intent = if (ActivityHelper.count === 0) {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent.`package` = packageName
                intent.putExtra("uPush.target", NewsDetailActivity::class.java)
                intent
            } else {
                val intent = Intent()
                intent.setClass(context, NewsDetailActivity::class.java)
                intent
            }

            intent.putExtra(Extras.DATA, data)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

        } catch (e: Exception) {
            Trace.e(e)
        }
    }

    fun resetPush(enable: Boolean) {
        val user = Store.store.getUser(this)
        val uToken = Store.store.getUToken(this)
        if (user?.uid != null && uToken != null) {
            val token = if (enable) uToken else ""
            SoguApi.getService(this)
                    .saveUser(uid = user.uid!!, advice_token = token)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        Trace.i("pushToken", "${payload.isOk}")
                    }, { e -> Trace.e(e) })
        }
    }

    companion object {
        lateinit var INSTANCE: App
    }
}