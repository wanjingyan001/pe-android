package com.sogukj.pe.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.system.text.ShortMessage
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QQClientNotExistException
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import cn.sharesdk.wechat.utils.WechatClientNotExistException
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EquityListBean
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.StructureBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sougukj.setOnClickFastListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_equity_structure.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.HashMap

/**
 * Created by qinfei on 17/8/11.
 */
class EquityStructureActivity : ToolbarActivity(), PlatformActionListener {
    lateinit var bean: EquityListBean
    lateinit var inflater: LayoutInflater
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bean = intent.getSerializableExtra(Extras.DATA) as EquityListBean
        setContentView(R.layout.activity_equity_structure)
        setBack(true)
        setTitle(bean.title)
        lunci.text = "轮次：${bean.lunci}"
        inflater = LayoutInflater.from(context)
        SoguApi.getService(application)
                .equityInfo(bean.hid!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        data?.forEach {
                            val ll_node = inflater.inflate(R.layout.item_equity_structure, null)
                            val llHeader = ll_node.findViewById(R.id.fl_header) as FrameLayout
                            val llChildren = ll_node.findViewById(R.id.ll_children) as LinearLayout
                            var tvName = ll_node.findViewById(R.id.tv_name) as TextView
                            var tvPercent = ll_node.findViewById(R.id.tv_percent) as TextView
                            var tvAmount = ll_node.findViewById(R.id.tv_amount) as TextView
                            tvName.text = it.name
                            tvPercent.text = it.percent
                            tvAmount.text = it.amount
//                            val cbxHeader = ll_node.findViewById(R.id.cbx_header) as CheckBox
//                            cbxHeader.setOnCheckedChangeListener { buttonView, isChecked ->
//                                if (isChecked) {
//                                    if (children == null || children?.size == 0) {
//                                        llChildren.visibility = View.GONE
//                                    } else {
//                                        llChildren.visibility = View.VISIBLE
//                                    }
//                                } else {
//                                    llChildren.visibility = View.GONE
//                                }
//                            }
                            if (it.children == null || it.children?.size == 0) {
                                llChildren.visibility = View.GONE
                            } else {
                                llChildren.visibility = View.VISIBLE
                            }
                            it.children?.apply {
                                setChildren(llChildren, this, 0)
                            }
                            nestedRoot.addView(ll_node)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "查询失败")
                })

        toolbar_menu.visibility = View.VISIBLE
        toolbar_menu.setImageResource(R.drawable.share)
        toolbar_menu.setOnClickListener {
            if (Utils.saveNestedScroolViewImage(mNestedScrollView, Environment.getExternalStorageDirectory().absolutePath)) {
                share()
            } else {
                showCustomToast(R.drawable.icon_toast_common, "生成截图失败")
            }
        }
    }

    val PATH  = Environment.getExternalStorageDirectory().absolutePath + "/EquityStructure.png"

    fun setChildren(llChildren: LinearLayout, dataList: List<StructureBean>, index: Int) {
        val index = index + 1
        dataList.forEach {
            val node = View.inflate(this@EquityStructureActivity, R.layout.item_equity_structure, null)
            llChildren.addView(node)
            val llHeader = node.findViewById(R.id.fl_header) as FrameLayout
            val llChildren = node.findViewById(R.id.ll_children) as LinearLayout

            setHeader(llHeader, it, index)
            if (index <= 3)
                it.children?.apply {
                    //                    val cbxHeader = node.findViewById(R.id.cbx_header) as CheckBox
//                    cbxHeader.setOnCheckedChangeListener { buttonView, isChecked ->
//                        if (isChecked) {
//                            if (this == null || this?.size == 0) {
//                                llChildren.visibility = View.GONE
//                            } else {
//                                llChildren.visibility = View.VISIBLE
//                            }
//                        } else {
//                            llChildren.visibility = View.GONE
//                        }
//                    }
                    if (this == null || this?.size == 0) {
                        llChildren.visibility = View.GONE
                    } else {
                        llChildren.visibility = View.VISIBLE
                    }
                    setChildren(llChildren, this, index)
                }
        }
    }

    val headers = arrayOf(R.drawable.bg_header_0, R.drawable.bg_header_1, R.drawable.bg_header_2, R.drawable.bg_header_3)

    fun setHeader(llHeader: View, data: StructureBean, index: Int) {
        llHeader.setBackgroundResource(headers[index])
        var llContent = llHeader.findViewById(R.id.ll_content)
        var tvName = llHeader.findViewById(R.id.tv_name) as TextView
        var tvPercent = llHeader.findViewById(R.id.tv_percent) as TextView
        var tvAmount = llHeader.findViewById(R.id.tv_amount) as TextView

        llContent.visibility = View.VISIBLE
        tvName.text = data.name
        tvPercent.text = data.percent
        tvAmount.text = data.amount
    }

    companion object {
        fun start(ctx: Activity?, bean: EquityListBean) {
            val intent = Intent(ctx, EquityStructureActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    private fun share(){
        val dialog = Dialog(this@EquityStructureActivity, R.style.AppTheme_Dialog)
        dialog.setContentView(R.layout.dialog_share)
        val lay = dialog.window.attributes
        lay.height = WindowManager.LayoutParams.WRAP_CONTENT
        lay.width = WindowManager.LayoutParams.MATCH_PARENT
        lay.gravity = Gravity.BOTTOM
        dialog.window.attributes = lay
        dialog.show()
        dialog.find<TextView>(R.id.tv_copy).visibility = View.GONE

        dialog.find<TextView>(R.id.tv_wexin).setOnClickFastListener {
            dialog.dismiss()
            val sp = cn.sharesdk.framework.Platform.ShareParams()
            sp.shareType = cn.sharesdk.framework.Platform.SHARE_IMAGE
            //sp.title = "我的名片"
            //sp.text = "这是${userBean.name}的名片"
            sp.imagePath = PATH
            val wechat = ShareSDK.getPlatform(Wechat.NAME)
            wechat.platformActionListener = this@EquityStructureActivity
            wechat.share(sp)
        }
        dialog.find<TextView>(R.id.tv_qq).setOnClickFastListener {
            dialog.dismiss()
            val sp = Platform.ShareParams()
            //sp.title = "我的名片"
            //sp.text = "这是${userBean.name}的名片"
            sp.imagePath = PATH
            val qq = ShareSDK.getPlatform(QQ.NAME)
            qq.platformActionListener = this@EquityStructureActivity
            qq.share(sp)
        }
    }

    override fun onComplete(p0: Platform?, p1: Int, p2: HashMap<String, Any>?) {
        runOnUiThread {
            p0?.let {
                when(it.name){
                    Wechat.NAME -> showCustomToast(R.drawable.icon_toast_success, "微信分享成功")
                    QQ.NAME -> showCustomToast(R.drawable.icon_toast_success, "QQ分享成功")
                }
            }
        }
    }

    override fun onCancel(p0: Platform?, p1: Int) {
        runOnUiThread {
            showCustomToast(R.drawable.icon_toast_common, "取消分享")
        }
    }

    override fun onError(p0: Platform?, p1: Int, p2: Throwable?) {
        runOnUiThread {
            p2?.let {
                it.printStackTrace()
                if (it is WechatClientNotExistException){
                    showCustomToast(R.drawable.icon_toast_common, "请安装微信")
                } else if (it is QQClientNotExistException || it is cn.sharesdk.tencent.qq.QQClientNotExistException){
                    showCustomToast(R.drawable.icon_toast_common, "请安装QQ")
                }else{
                    showCustomToast(R.drawable.icon_toast_fail, "分享失败")
                }
            }
        }
    }
}
