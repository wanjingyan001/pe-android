package com.sogukj.pe.ui

import android.os.Bundle
import android.view.View
import com.framework.base.ToolbarFragment
import com.sogukj.pe.R

/**
 * Created by qinfei on 17/10/11.
 */
class MainMsgFragment : ToolbarFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_msg_center

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle("消息中心")
    }

    companion object {
        val TAG = MainMsgFragment::class.java.simpleName

        fun newInstance(): MainMsgFragment {
            val fragment = MainMsgFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}