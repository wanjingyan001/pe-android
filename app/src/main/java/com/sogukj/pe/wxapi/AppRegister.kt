package com.sogukj.pe.wxapi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tencent.mm.sdk.openapi.WXAPIFactory


class AppRegister : BroadcastReceiver() {
    var WEECHAT_ID = "wxf3a9c44cb15f4bc2"

    override fun onReceive(context: Context, intent: Intent) {
        val msgApi = WXAPIFactory.createWXAPI(context, null)

        // 将该app注册到微信
        msgApi.registerApp(WEECHAT_ID)
    }
}
