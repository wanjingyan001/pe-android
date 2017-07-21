package com.framework.base

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast
import com.framework.util.Trace
import org.greenrobot.eventbus.Subscribe

/**
 * Created by qinfei on 17/7/17.
 */
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
        ActivityHelper.add(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //        initStatusBar(getStatusBarTintRes());
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

    fun showProgress(msg: String) {
    }

    fun showProgress(msg: String, theme: Int) {
    }

    fun hideProgress() {
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
