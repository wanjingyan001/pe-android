package com.framework.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import org.greenrobot.eventbus.Subscribe

abstract class BaseFragment : android.support.v4.app.Fragment() {

    abstract val containerViewId: Int

    var baseActivity: BaseActivity? = null
        private set

    private var toast: Toast? = null

    val titleId: Int
        get() = 0

//    fun uiThread(task: Runnable?) {
//        if (null != baseActivity && task != null)
//            baseActivity!!.runOnUiThread {
//                try {
//                    task.run()
//                } catch (e: Exception) {
//                    Trace.e(javaClass.simpleName, "", e)
//                }
//            }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivity = activity as BaseActivity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (containerViewId != 0) {
            val view = inflater!!.inflate(containerViewId, container, false)
            return view
        } else {
            return super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    fun showToast(text: CharSequence) {
        if (toast == null) {
            toast = Toast.makeText(activity,
                    text,
                    Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(text)
        }
        toast!!.show()
    }

    @Subscribe
    internal fun onEmptyEvent() {

    }
}
