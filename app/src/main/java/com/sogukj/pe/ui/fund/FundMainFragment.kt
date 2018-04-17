package com.sogukj.pe.ui.fund

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.FundSmallBean.Companion.FundAsc
import com.sogukj.pe.bean.FundSmallBean.Companion.FundDesc
import com.sogukj.pe.bean.FundSmallBean.Companion.RegTimeAsc
import com.sogukj.pe.bean.FundSmallBean.Companion.RegTimeDesc
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.main.MainActivity
import com.sogukj.pe.ui.user.UserActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_main.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource

class FundMainFragment : BaseFragment(), View.OnClickListener {
    override val containerViewId: Int
        get() = R.layout.activity_fund_main

    companion object {
        val TAG: String = FundMainFragment::class.java.simpleName

        fun newInstance(): FundMainFragment {
            val fragment = FundMainFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    val fragments = arrayOf(
            FundListFragment.newInstance(FundListFragment.TYPE_CB),
            FundListFragment.newInstance(FundListFragment.TYPE_CX),
            FundListFragment.newInstance(FundListFragment.TYPE_TC)
    )

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            loadHead()
        }
    }

    fun loadHead() {
        val user = Store.store.getUser(baseActivity!!)
//        if (user?.url.isNullOrEmpty()) {
//            toolbar_back.setChar(user?.name?.first())
//        } else {
//            Glide.with(context).load(user?.url).into(toolbar_back)
//        }
        if (user?.url.isNullOrEmpty()) {
            val ch = user?.name?.first()
            toolbar_back.setChar(ch)
        } else {
            Glide.with(context)
                    .load(user?.url)
                    .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                    .into(toolbar_back)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x789) {
            loadHead()
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fundTitle.text = "基金"

        run {
            ll_order_name_1.setOnClickListener(this)
            ll_order_time_1.setOnClickListener(this)
//            iv_user.setOnClickListener(this)
            iv_search.setOnClickListener(this)
        }

        loadHead()
        toolbar_back.setOnClickListener {
            //UserActivity.start(context)
            val intent = Intent(context, UserActivity::class.java)
            startActivityForResult(intent, 0x789)
        }

        run {
            var adapter = ArrayPagerAdapter(childFragmentManager, fragments)
            view_pager.adapter = adapter
            view_pager.offscreenPageLimit = fragments.size
            var transFormer = ProjectPageTransformer()
            //view_pager.setPageTransformer(true, transFormer)

            tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    view_pager?.currentItem = tab.position
                }

            })
            view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    //Log.e("PageScrollState", "${state}")
                    if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                        judgeOncce = 0
                    }
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    //position永远都是两个的左边一个，viewpager往右滑，positionOffset从0到1（到达1以后position++）
                    //Log.e("onPageScrolled", "${position} +++ ${positionOffset} +++ ${positionOffsetPixels}")
                    if (judgeOncce == 0) {
                        if (positionOffset > 0.5f) {
                            transFormer.setDirection(true)
                        } else if (positionOffset < 0.5f) {
                            transFormer.setDirection(false)
                        }
                        judgeOncce = 1
                    }
                }

                //滑到新页面才算，没滑到又返回不算
                override fun onPageSelected(position: Int) {
                    //Log.e("onPageSelected", "onPageSelected")
                    tabs?.getTabAt(position)?.select()
                    changeView()
                    setContent()
                }

            })
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            setContent()
        } catch (e: Exception) {
        }
    }

    private fun changeView() {
        if (toolbarLayout.height == 0) {//toolbarLayout.height=0  fragment来回切换导致toolbarLayout还没有宽高就要
            tabs.setBackgroundResource(R.drawable.tab_bg_1)
            tabs.setTabTextColors(Color.parseColor("#a0a4aa"), Color.parseColor("#282828"))
            for (i in 0 until tabs.getTabCount()) {
                if (i == tabs.getSelectedTabPosition()) {
                    setDrawable(i, "1", true)
                } else {
                    setDrawable(i, "1", false)
                }
            }
        } else {
            if (MyNestedScrollParentFund.scrollY < toolbarLayout.height) {//如果parent外框，还可以往下滑动
                tabs.setBackgroundResource(R.drawable.tab_bg_1)
                tabs.setTabTextColors(Color.parseColor("#a0a4aa"), Color.parseColor("#282828"))
                for (i in 0 until tabs.getTabCount()) {
                    if (i == tabs.getSelectedTabPosition()) {
                        setDrawable(i, "1", true)
                    } else {
                        setDrawable(i, "1", false)
                    }
                }
            } else if (MyNestedScrollParentFund.scrollY >= toolbarLayout.height) {
                tabs.setBackgroundResource(R.drawable.tab_bg_2)
                tabs.setTabTextColors(Color.parseColor("#ff7bb4fc"), Color.parseColor("#ffffff"))
                for (i in 0 until tabs.getTabCount()) {
                    if (i == tabs.getSelectedTabPosition()) {
                        setDrawable(i, "2", true)
                    } else {
                        setDrawable(i, "2", false)
                    }
                }
            }
        }
    }

    /**
     * @param index--------(tabs对应的index，分别对应dy,cb等)
     * @param state---------（1，2）
     * @param isSelect--------是否选中
     */
    private fun setDrawable(index: Int, state: String, isSelect: Boolean) {
        var name = ""
        when (index) {
            0 -> name += "cb_"
            1 -> name += "cx_"
            2 -> name += "tc_"
        }
        name += state
        if (isSelect) {
            name += "_select"
        } else {
            name += "_unselect"
        }
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        tabs.getTabAt(index)!!.setIcon(id)
    }

    /**
     * 不知道为啥能成功
     */
    private fun setContent() {
        var view = fragments.get(view_pager.currentItem).getRecycleView()
        //RecycleView可能还没渲染，这样第一个setCurrentContentView失效
        view.post(object : Runnable {
            override fun run() {
                if (view.height > 0) {
                    MyNestedScrollParentFund.setCurrentContentView(view)
                }
            }
        })
    }

    var judgeOncce = 0

    override fun onClick(p0: View?) {
        when (p0?.id) {
//            R.id.ll_order_name_1 -> {
//                if (currentNameOrder == FundDesc) {
//                    currentNameOrder = FundAsc
//                    iv_sort_name_1.imageResource = R.drawable.ic_up
//                } else {
//                    currentNameOrder = FundDesc
//                    iv_sort_name_1.imageResource = R.drawable.ic_down
//                }
//                page = 0
//                doRequest()
//            }
//            R.id.ll_order_time_1 -> {
//                if (currentTimeOrder == RegTimeAsc) {
//                    currentTimeOrder = RegTimeDesc
//                    iv_sort_time_1.imageResource = R.drawable.ic_down
//                } else {
//                    currentTimeOrder = RegTimeAsc
//                    iv_sort_time_1.imageResource = R.drawable.ic_up
//                }
//                page = 0
//                doRequest()
//            }
//            R.id.iv_user -> {
//                val activity = activity as MainActivity
//                activity.find<RadioGroup>(R.id.rg_tab_main).check(R.id.rb_my)
////                UserFragment.start(activity)
//            }
            R.id.iv_search -> {
                var type = view_pager.currentItem + 1
                FundSearchActivity.start(activity, type)
            }
        }

    }
}
