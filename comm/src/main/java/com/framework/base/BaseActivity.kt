package com.framework.base

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast
import com.framework.util.Trace
import org.greenrobot.eventbus.Subscribe


abstract class BaseActivity : AppCompatActivity() {

    private var prDialog: ProgressDialog? = null

    enum class Animat {
        NONE,
        SLDE,
        FADE,
        SLDE_UP
    }

    val context: BaseActivity
        get() = this

//    fun uiThread(task: Runnable?) {
//        if (null != task)
//            runOnUiThread(task)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityManager.add(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //        initStatusBar(getStatusBarTintRes());
    }

    override fun onStart() {
        super.onStart()
        activeCount++
        val intent = Intent()
        intent.setClassName(baseContext, "cn.sogukj.stockalert.webservice.DzhClient")
        startService(intent)
    }

    override fun onStop() {
        super.onStop()
        activeCount--
        if (activeCount == 0) {
            val intent = Intent()
            intent.setClassName(baseContext, "cn.sogukj.stockalert.webservice.DzhClient")
            stopService(intent)
        }

    }

    //    //初始化statusbar
    //    protected void initStatusBar(int statusbar) {
    //        if (statusbar != 0) {
    //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    //                setTranslucentStatus(true, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    //            }
    //
    //            SystemBarTintManager tintManager = new SystemBarTintManager(this);
    //            tintManager.setStatusBarTintEnabled(true);
    //            tintManager.setStatusBarTintColor(statusbar);
    //        }
    //    }


    //获取statusbar高度
    val statusBarSize: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }


    //获取screen高度
    val screenHeight: Int
        get() = windowManager.defaultDisplay.height

    val screenWidth: Int
        get() = windowManager.defaultDisplay.width

    fun showProgress(msg: String) {
        // TODO Auto-generated method stub
        if (prDialog == null) {
            prDialog = MsgDialog.progress(this, msg)
            prDialog!!.setCancelable(false)
        } else {
            prDialog!!.setMessage(msg)
        }
        prDialog!!.show()
    }

    fun showProgress(msg: String, theme: Int) {
        // TODO Auto-generated method stub
        if (prDialog == null) {
            prDialog = MsgDialog.progress(this, msg, theme)
            prDialog!!.setCancelable(false)
        } else {
            prDialog!!.setMessage(msg)
        }
        prDialog!!.show()
    }

    fun hideProgress() {
        // TODO Auto-generated method stub
        if (prDialog != null) {
            if (prDialog!!.isShowing) {
                prDialog!!.dismiss()
                prDialog = null
            }
        }
    }


    override fun onResume() {
        super.onResume()
        ActivityManager.curActivity = this
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        ActivityManager.remove(this)
        hideProgress()
        super.onDestroy()
    }

    fun showToast(text: CharSequence?) {
        Trace.d(TAG, text?.toString())
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }


    @Subscribe
    internal fun onEmptyEvent() {

    }

    companion object {
        val TAG = BaseActivity::class.java.simpleName
        var activeCount = 0
    }
}
