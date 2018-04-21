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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_equity_structure.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
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
            share()
        }
    }

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

    override fun onComplete(platform: Platform, i: Int, hashMap: HashMap<String, Any>) {
        if (platform.name == WechatMoments.NAME) {// 判断成功的平台是不是朋友圈
            mHandler.sendEmptyMessage(1)
        } else if (platform.name == Wechat.NAME) {
            mHandler.sendEmptyMessage(2)
        } else if (platform.name == SinaWeibo.NAME) {
            mHandler.sendEmptyMessage(3)
        } else if (platform.name == QQ.NAME) {
            mHandler.sendEmptyMessage(4)
        } else if (platform.name == QZone.NAME) {
            mHandler.sendEmptyMessage(5)
        } else if (platform.name == ShortMessage.NAME) {
            mHandler.sendEmptyMessage(6)
        }
    }

    override fun onError(platform: Platform, i: Int, throwable: Throwable) {
        throwable.printStackTrace()

        if (throwable is WechatClientNotExistException) {
            mHandler.sendEmptyMessage(8)
        } else if (throwable is PackageManager.NameNotFoundException) {
            mHandler.sendEmptyMessage(9)
        } else if (throwable is QQClientNotExistException || throwable is cn.sharesdk.tencent.qq.QQClientNotExistException) {
            mHandler.sendEmptyMessage(10)
        } else {
            mHandler.sendEmptyMessage(11)
        }

    }

    override fun onCancel(platform: Platform, i: Int) {
        mHandler.sendEmptyMessage(7)
    }

    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> Toast.makeText(this@EquityStructureActivity, "朋友圈分享成功", Toast.LENGTH_LONG).show()
                2 -> Toast.makeText(this@EquityStructureActivity, "微信分享成功", Toast.LENGTH_LONG).show()
                3 -> Toast.makeText(this@EquityStructureActivity, "新浪微博分享成功", Toast.LENGTH_LONG).show()
                4 -> Toast.makeText(this@EquityStructureActivity, "QQ分享成功", Toast.LENGTH_LONG).show()
                5 -> Toast.makeText(this@EquityStructureActivity, "QQ空间分享成功", Toast.LENGTH_LONG).show()
                6 -> Toast.makeText(this@EquityStructureActivity, "短信分享成功", Toast.LENGTH_LONG).show()
                7 -> Toast.makeText(this@EquityStructureActivity, "取消分享", Toast.LENGTH_LONG).show()
                8 -> Toast.makeText(this@EquityStructureActivity, "请安装微信", Toast.LENGTH_LONG).show()
                9 -> Toast.makeText(this@EquityStructureActivity, "请安装微博", Toast.LENGTH_LONG).show()
                10 -> Toast.makeText(this@EquityStructureActivity, "请安装QQ", Toast.LENGTH_LONG).show()
                11 -> Toast.makeText(this@EquityStructureActivity, "分享失败", Toast.LENGTH_LONG).show()
                else -> {
                }
            }
        }
    }

    var news: NewsBean? = null

    fun share() {
        if (news == null)
            return
        val dialog = Dialog(this, R.style.AppTheme_Dialog)
        dialog.setContentView(R.layout.dialog_share)
        val lay = dialog.getWindow()!!.getAttributes()
        lay.height = WindowManager.LayoutParams.WRAP_CONTENT
        lay.width = WindowManager.LayoutParams.MATCH_PARENT
        lay.gravity = Gravity.BOTTOM
        dialog.getWindow()!!.setAttributes(lay)
        dialog.show()

        val tvWexin = dialog.findViewById(R.id.tv_wexin) as TextView
        val tvQq = dialog.findViewById(R.id.tv_qq) as TextView
        val tvCopy = dialog.findViewById(R.id.tv_copy) as TextView
        val shareUrl = news!!.shareUrl
        val shareTitle = news!!.shareTitle
        val shareSummry = news!!.title
        //val shareImgUrl = File(Environment.getExternalStorageDirectory(), "img_logo.png").toString()
        val shareImgUrl:String
        when (Utils.getEnvironment()) {
            "civc" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_zd.png").toString()
            }
            "ht" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_ht.png").toString()
            }
            "kk" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_kk.png").toString()
            }
            "yge" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_yge.png").toString()
            }
            else -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "img_logo.png").toString()
            }
        }
        tvCopy.setOnClickListener {
            dialog.dismiss()
            Utils.copy(this, shareUrl)
            showCustomToast(R.drawable.icon_toast_common, "已复制")
        }
        tvQq.setOnClickListener {
            dialog.dismiss()
            val sp = Platform.ShareParams()
            sp.setTitle(shareTitle)
            sp.setText(shareSummry)
            sp.setImageUrl(shareImgUrl)//网络图片rul
            sp.setTitleUrl(shareUrl)
            val qq = ShareSDK.getPlatform(QQ.NAME)
            qq.platformActionListener = this
            qq.share(sp)
        }
        tvWexin.setOnClickListener {
            dialog.dismiss()
            val sp = cn.sharesdk.framework.Platform.ShareParams()
            sp.setShareType(cn.sharesdk.framework.Platform.SHARE_WEBPAGE)//非常重要：一定要设置分享属性
            sp.setTitle(shareTitle)  //分享标题
            sp.setText(shareSummry)   //分享文本
            if (null != news!!.imgUrl) {
                sp.imageUrl = shareImgUrl//网络图片rul
            } else {
                sp.imagePath = shareImgUrl//
            }
            sp.setUrl(shareUrl)
            val wechat = ShareSDK.getPlatform(Wechat.NAME)
            wechat.platformActionListener = this
            wechat.share(sp)
        }

    }
}
