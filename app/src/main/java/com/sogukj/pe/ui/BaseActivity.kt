package com.framework.base

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.util.Trace
import com.umeng.message.PushAgent
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by qinfei on 17/7/17.
 */
abstract class BaseActivity : AppCompatActivity() {
    val context: BaseActivity
        get() = this

    val handler = Handler();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHelper.add(this)
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //        initStatusBar(getStatusBarTintRes());
        PushAgent.getInstance(this).onAppStart();
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
            progressDialog?.setMessage(msg)
            progressDialog?.show()
        }
    }

    fun showProgress(msg: String, theme: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog?.setMessage(msg)
            progressDialog?.setProgressStyle(theme)
            progressDialog?.show()
        }
    }

    fun hideProgress() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
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
        when (e) {
            is JsonSyntaxException -> showToast("后台数据出错")
            is UnknownHostException -> showToast("网络出错")
            is SocketTimeoutException -> showToast("连接超时")
            else -> showToast("未知错误")
        }
    }

    companion object {
        val TAG = BaseActivity::class.java.simpleName
        var activeCount = 0
    }
}
