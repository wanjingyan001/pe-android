package com.sogukj.pe

import android.content.Context
import android.content.Intent
import android.support.multidex.MultiDexApplication
import com.framework.base.ActivityHelper
import com.google.gson.Gson
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.ui.NewsDetailActivity
import com.sogukj.pe.util.Trace
import com.sogukj.util.Store
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import org.json.JSONObject


/**
 * Created by qinfei on 17/7/18.
 */
class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        val mPushAgent = PushAgent.getInstance(this)
        mPushAgent.setDebugMode(false)
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
               uMessage.custom?.apply {
                   com.sogukj.pe.util.Trace.i("uPush", this)
                   val json = JSONObject(this)
                   val type = json.getInt("type")
                   when (type) {
                       else -> handle(context, json)
                   }
               }
           }catch (e:Exception){
               Trace.e(e)
           }
        })
    }

    val GSON = Gson();
    fun handle(context: Context, json: JSONObject) {
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
    }

    companion object {
        lateinit var INSTANCE: App
    }
}