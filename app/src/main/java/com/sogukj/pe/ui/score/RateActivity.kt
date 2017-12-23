package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.view.ArrayPagerAdapter
import kotlinx.android.synthetic.main.activity_rate.*
import org.jetbrains.anko.textColor
import com.lcodecore.tkrefreshlayout.utils.DensityUtil
import android.support.v4.view.MarginLayoutParamsCompat.setMarginEnd
import android.support.v4.view.MarginLayoutParamsCompat.setMarginStart
import android.widget.LinearLayout
import com.framework.base.BaseFragment
import com.sogukj.pe.Extras
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.item_comment_list.*
import java.lang.reflect.AccessibleObject.setAccessible


class RateActivity : ToolbarActivity() {

    companion object {
        /**
         * check_person 被评分人信息
         * isShow-- 是否展示页面  true为展示页面，false是打分界面
         */
        fun start(ctx: Context?, check_person: GradeCheckBean.ScoreItem, isShow: Boolean) {
            val intent = Intent(ctx, RateActivity::class.java)
            intent.putExtra(Extras.DATA, check_person)
            intent.putExtra(Extras.FLAG, isShow)
            ctx?.startActivity(intent)
        }
    }

    lateinit var person: GradeCheckBean.ScoreItem
    var isShow = false

    lateinit var fragments: Array<BaseFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate)

        setBack(true)
        setTitle("考核评分")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        person = intent.getSerializableExtra(Extras.DATA) as GradeCheckBean.ScoreItem
        isShow = intent.getBooleanExtra(Extras.FLAG, false) // = false 打分界面，true展示界面

        //1=>其他模版 2=>风控部模版 3=>投资部模版
        if (person.type == 3) {
            fragments = arrayOf(
                    InvestManageFragment.newInstance(person, isShow)
            )
        } else if (person.type == 2) {
            fragments = arrayOf(
                    FengKongFragment.newInstance(person, isShow)
            )
        } else if (person.type == 1) {
            fragments = arrayOf(
                    RateFragment.newInstance(person, isShow)
            )
        } else {
            fragments = arrayOf(
            )
        }

        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = adapter
    }
}
