package com.framework.base

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

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
}
