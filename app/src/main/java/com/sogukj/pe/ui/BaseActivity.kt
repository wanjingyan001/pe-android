package com.framework.base

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.annotation.DrawableRes
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.util.Trace
import com.umeng.message.PushAgent
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.sogukj.pe.R
import com.sogukj.pe.util.OnClickFastListener
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource

/**
 * Created by qinfei on 17/7/17.
 */
abstract class BaseActivity : AppCompatActivity(),AnkoLogger {
    val context: BaseActivity
        get() = this


    val handler = Handler();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHelper.add(this)
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //        initStatusBar(getStatusBarTintRes());
        PushAgent.getInstance(this).onAppStart();
        initEmptyView()

        inflate = layoutInflater.inflate(R.layout.layout_custom_toast, null)
        icon = inflate.find<ImageView>(R.id.toast_icon)
        tv = inflate.find<TextView>(R.id.toast_tv)
    }

    override fun onStart() {
        super.onStart()
        activeCount++
    }

    override fun onStop() {
        super.onStop()
        activeCount--
        if (activeCount == 0) {
        }

    }


    val statusBarSize: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }


    val screenHeight: Int
        get() = windowManager.defaultDisplay.height

    val screenWidth: Int
        get() = windowManager.defaultDisplay.width

    var progressDialog: ProgressDialog? = null
    fun showProgress(msg: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
        }
        progressDialog?.setMessage(msg)
        progressDialog?.show()
    }

    fun showProgress(msg: String, theme: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
        }
        progressDialog?.setMessage(msg)
        progressDialog?.setProgressStyle(theme)
        progressDialog?.show()
    }

    fun hideProgress() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }

    var layout: ConstraintLayout? = null
    private fun initEmptyView() {
        if (findViewById(R.id.networkErrorLayout) != null) {
            layout = find(R.id.networkErrorLayout)
            if (layout == null) {
                return
            } else {
                layout?.let {
                    it.visibility = View.GONE
                }
            }
        }
    }

    protected fun showEmptyView(){
        layout?.let {
            it.visibility = View.VISIBLE
        }
    }

    protected fun hideEmptyView(){
        layout?.let {
            it.visibility = View.GONE
        }
    }


    override fun onResume() {
        super.onResume()
        ActivityHelper.curActivity = this
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        ActivityHelper.remove(this)
        hideProgress()
        super.onDestroy()
    }


    private var toast: Toast? = null
    fun showToast(text: CharSequence?) {
        Trace.d(TAG, text?.toString())
        //文件上传未完成，界面销毁
        if (!(context is Activity)) {
            return
        }
        if (toast == null) {
            toast = Toast.makeText(this,
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

    lateinit var inflate:View
    lateinit var icon:ImageView
    lateinit var tv:TextView
    private var toastView: Toast? = null

    fun showCustomToast(@DrawableRes resId: Int?, text: CharSequence?) {
//        val inflate = layoutInflater.inflate(R.layout.layout_custom_toast, null)
//        val icon = inflate.find<ImageView>(R.id.toast_icon)
//        val tv = inflate.find<TextView>(R.id.toast_tv)

        if (!(context is Activity)) {
            return
        }

        if (resId != null) {
            icon.imageResource = resId
        } else {
            icon.visibility = View.GONE
        }
        tv.maxWidth = screenWidth / 3
        tv.text = text
        if (toastView == null) {
            toastView = Toast(this)
        }
        toastView?.let {
            it.setGravity(Gravity.CENTER_VERTICAL, 0, -50)
            it.duration = Toast.LENGTH_SHORT
            it.view = inflate
            it.show()
        }
    }

    companion object {
        val TAG = BaseActivity::class.java.simpleName
        var activeCount = 0
    }
}
