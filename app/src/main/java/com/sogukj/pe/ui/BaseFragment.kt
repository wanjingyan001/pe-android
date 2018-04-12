package com.framework.base

import android.os.Bundle
import android.os.Handler
import android.support.annotation.DrawableRes
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.R
import com.sogukj.pe.util.OnClickFastListener
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by qinfei on 17/7/18.
 */

abstract class BaseFragment : android.support.v4.app.Fragment() {

    abstract val containerViewId: Int
    val handler = Handler();
    var baseActivity: BaseActivity? = null
        private set



    open val titleId: Int
        get() = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivity = activity as BaseActivity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        inflateView = layoutInflater.inflate(R.layout.layout_custom_toast, null)
        icon = inflateView.find<ImageView>(R.id.toast_icon)
        tv = inflateView.find<TextView>(R.id.toast_tv)

        if (containerViewId != 0) {
            val view = inflater!!.inflate(containerViewId, container, false)
            return view
        } else {
            return super.onCreateView(inflater, container, savedInstanceState)
        }
    }
    private var toast: Toast? = null
    fun showToast(text: CharSequence?) {
        if (toast == null) {
            toast = Toast.makeText(activity,
                    text?.toString(),
                    Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(text)
        }
        toast!!.show()
    }

    fun ToastError(e:Throwable){
        var str = when (e) {
            is JsonSyntaxException -> "后台数据出错"
            is UnknownHostException -> "网络连接出错，请联网"
            is SocketTimeoutException -> "连接超时"
            else -> "未知错误"
        }
        showCustomToast(R.drawable.icon_toast_fail, str)
    }

    lateinit var inflateView:View
    lateinit var icon: ImageView
    lateinit var tv: TextView
    private var toastView: Toast? = null

    fun showCustomToast(@DrawableRes resId: Int?, text: CharSequence?) {
//        val inflate = layoutInflater.inflate(R.layout.layout_custom_toast, null)
//        val icon = inflate.find<ImageView>(R.id.toast_icon)
//        val tv = inflate.find<TextView>(R.id.toast_tv)
        if (resId != null) {
            icon.imageResource = resId
        } else {
            icon.visibility = View.GONE
        }
        var width = baseActivity!!.windowManager.defaultDisplay.width
        tv.maxWidth = width / 3
        tv.text = text
        if (toastView == null) {
            toastView = Toast(activity)
        }
        toastView?.let {
            it.setGravity(Gravity.CENTER_VERTICAL, 0, -50)
            it.duration = Toast.LENGTH_SHORT
            it.view = inflateView
            it.show()
        }
    }
}
