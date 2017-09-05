package com.framework.base

import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.sogukj.pe.R

/**
 * Created by qinfei on 17/7/18.
 */
abstract class ToolbarActivity : BaseActivity() {

    open  val titleId: Int
        get() = 0

    open val menuId: Int
        get() = 0

    val displayHomeAsUpEnabled: Boolean
        get() = false

    var toolbar: Toolbar? = null
        private set

    fun setBack(visible: Boolean) {
        toolbar?.apply {
            val back = this.findViewById(R.id.toolbar_back) as View?
            back?.setVisibility(if (visible) View.VISIBLE else View.INVISIBLE)
            back?.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initToolbar() {
        if (findViewById(R.id.toolbar) == null) {
            return
        }

        toolbar = findViewById(R.id.toolbar) as Toolbar?
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            if (title != null && titleId != 0)
                title.setText(titleId)
            setSupportActionBar(this)
            supportActionBar?.setDisplayShowTitleEnabled(false)
//            supportActionBar?.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled)

            this.background.alpha = 255
            this.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
    }

    val actionBarSize: Int
        get() {
            val typedValue = TypedValue()
            val textSizeAttr = intArrayOf(R.attr.actionBarSize)
            val indexOfAttrTextSize = 0
            val a = obtainStyledAttributes(typedValue.data, textSizeAttr)
            val actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1)
            a.recycle()
            return actionBarSize
        }

    override fun setTitle(resId: Int) {
        toolbar?.apply {
            if (toolbar?.findViewById(R.id.toolbar_title) != null) {
                if (resId != 0) {
                    (toolbar?.findViewById(R.id.toolbar_title) as TextView).setText(resId)
                }
            }
        }
    }

    override fun setTitle(titleRes: CharSequence?) {
        if (toolbar != null) {
            if (toolbar?.findViewById(R.id.toolbar_title) != null) {
                if (titleRes != null) {
                    (toolbar?.findViewById(R.id.toolbar_title) as TextView).text = titleRes
                }
            }
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initToolbar()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        initToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        if (menuId != 0) {
            menuInflater.inflate(menuId, menu)
            return true
        }

        return super.onCreateOptionsMenu(menu)
    }
}
