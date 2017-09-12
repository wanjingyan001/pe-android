package com.sogukj.pe

import android.support.multidex.MultiDexApplication
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent


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
            }

            override fun onFailure(s: String, s1: String) {
                println("IUmengRegisterCallback:$s=>$s1")
            }
        })
    }

    companion object {
        lateinit var INSTANCE: App
    }
}