package com.sogukj.pe.ui.partyBuild

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.widget.TextView
import com.framework.base.BaseActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.PartyTabBean
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_party_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find

class PartyMainActivity : BaseActivity() {
    lateinit var adapter: PartyAdapter

    companion object {
        val ARTICLE = 1
        val FILE = 2
        val TABS: String = "tabsKey"
        fun start(context: Context) {
            val intent = Intent(context, PartyMainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_main)
        Utils.setWindowStatusBarColor(this, R.color.party_toolbar_red)
        toolbar.background = resources.getDrawable(R.color.party_toolbar_red)
        toolbar_title.text = "党建专栏"
        categoryList()
        addTv.setOnClickListener {
            PartyUploadActivity.start(this)
        }
        back.setOnClickListener {
            finish()
        }
    }


    private fun initPager(tabs: List<PartyTabBean>) {
        adapter = PartyAdapter(supportFragmentManager, tabs)
        contentPager.adapter = adapter
        tabLayout.setupWithViewPager(contentPager)
        //自定义Indicator
        for (i in 0 until tabs.size) {
            val tab = tabLayout.getTabAt(i)
            tab?.let {
                it.setCustomView(R.layout.layout_party_custom_indicator)
                if (i == 0) {
                    it.customView!!.isSelected = true
                }
                it.customView!!.find<TextView>(R.id.indicatorTv).text = tabs[i].classname
            }
        }
    }

//    private fun customIndicator(bean: PartyTabBean) = relativeLayout {
//        textView(bean.classname) {
//            textSize = sp(14f).toFloat()
//            textColor = R.drawable.color_party_tab
//        }.lparams(width = wrapContent, height = dip(36)) { centerInParent() }
//        view {
//            backgroundResource = R.drawable.bg_party_tab
//        }.lparams(width = dip(20), height = dip(2)) {
//            alignParentBottom()
//            centerHorizontally()
//        }
//    }

    private fun categoryList() {
        SoguApi.getService(application)
                .categoryList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            XmlDb.open(ctx).set(TABS, Gson().toJson(it))
                            initPager(it)
                        }
                    }
                })
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE) {
            supportFragmentManager.fragments.forEach {
                if (it.userVisibleHint && it is PartyListFragment) {
                    it.doRequest()
                }
            }
        }
    }


    inner class PartyAdapter(fm: FragmentManager?, val tabs: List<PartyTabBean>) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            val bean = tabs[position]
            return PartyListFragment.newInstance(bean.id, bean.classname, bean.picture)
        }

        override fun getCount(): Int = tabs.size

        override fun getPageTitle(position: Int): CharSequence {
            return tabs[position].classname!!
        }
    }
}
