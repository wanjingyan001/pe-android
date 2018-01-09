package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import cn.finalteam.rxgalleryfinal.RxGalleryFinal
import cn.finalteam.rxgalleryfinal.imageloader.ImageLoaderType
import cn.finalteam.rxgalleryfinal.rxbus.RxBusResultDisposable
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageRadioResultEvent
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.score.*
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_edit.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.*
import java.net.UnknownHostException


/**
 * Created by qinfei on 17/7/18.
 */

class UserEditActivity : ToolbarActivity() {

    override val menuId: Int
        get() = R.menu.user_edit
    var user: UserBean = UserBean()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)
        setBack(true)
        title = "个人信息"
        val departList = intent.getSerializableExtra(Extras.DATA) as ArrayList<DepartmentBean>?
        Store.store.getUser(this)?.apply {
            user = this
            if (!TextUtils.isEmpty(name)) {
                tv_name?.setText(name)
                tv_name?.isEnabled = false
            }
            if (!TextUtils.isEmpty(phone))
                tv_phone?.text = phone
            if (!TextUtils.isEmpty(email))
                tv_email?.setText(email)
            if (!TextUtils.isEmpty(position))
                tv_posotion?.setText(position)
            if (!TextUtils.isEmpty(depart_name))
                tv_depart?.text = depart_name
            if (!TextUtils.isEmpty(memo))
                tv_note?.setText(memo)
            if (!TextUtils.isEmpty(url))
                Glide.with(this@UserEditActivity)
                        .load(headImage())
//                        .error(R.drawable.img_logo_user)
                        .into(iv_user)
            if (full != null) {
                tv_resume.text = "简历完整度:${full}"
            }
        }

        tr_resume.setOnClickListener {
            UserResumeActivity.start(this, Store.store.getUser(this)!!)
        }
        tv_depart.setOnClickListener {
            val items = ArrayList<String?>()
            var position = -1
            departList?.forEach {
                items.add(it.de_name)
            }
            items.forEachIndexed { index, s ->
                if (s!! == tv_depart.text) {
                    position = index
                }
            }

            MaterialDialog.Builder(this@UserEditActivity)
                    .theme(Theme.LIGHT)
                    .title("选择部门")
                    .items(items)
                    .itemsCallbackSingleChoice(position, MaterialDialog.ListCallbackSingleChoice { dialog, v, p, s ->
                        if (p == -1) return@ListCallbackSingleChoice false
                        val data = departList?.get(p)
                        data?.apply {
                            user.depart_id = depart_id
                            user.depart_name = de_name
                        }
                        tv_depart.text = user.depart_name
                        true
                    })
                    .positiveText("确定")
                    .negativeText("取消")
                    .show()
        }
        tr_icon.setOnClickListener {
            //            //            val intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(intent, REQ_PHOTO)
            RxGalleryFinal
                    .with(this@UserEditActivity)
                    .image()
                    .radio()
//                    .cropMaxBitmapSize(1024 * 1024)
////                    .cropAspectRatioOptions(0, AspectRatio("1:1", 120f, 120f))
//                    .cropMaxResultSize(120, 120)
//                    .crop()
                    .imageLoader(ImageLoaderType.GLIDE)
                    .subscribe(object : RxBusResultDisposable<ImageRadioResultEvent>() {
                        override fun onEvent(event: ImageRadioResultEvent?) {
                            val path = event?.result?.originalPath
                            if (!TextUtils.isEmpty(path))
                                doUpload(path!!)
                        }
                    })
                    .openGallery()
        }

        //-1=>隐藏入口 0=>未开启  1=>进入评分中心，2=>进入填写页面
        tr_rate.setOnClickListener {
            if (TYPE == 1) {
                LeaderActivity.start(context)
            } else if (TYPE == 2) {
                TemplateActivity.start(context)
            }
        }
    }

    private val mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {
            if (msg.what == 0x001) {
                synchronized(this) {
                    var leftSec = msg.obj as Int
                    tv_rate.text = "距离结束还有${formatSec(leftSec)}"
                    leftSec--
                    if (leftSec == -1) {
                        tv_rate.text = "超时"
                    } else {
                        sendMessageDelayed(obtainMessage(0x001, leftSec), 1000)
                    }
                }
            }
        }
    }

    fun formatSec(sec: Int): String {
        var second = sec - sec / 60 * 60
        var minute = sec / 60 - sec / 60 / 60 * 60
//        var hour = sec / 60 / 60 - sec / 60 / 60 / 24 * 24
//        var day = sec / 60 / 60 / 24
        var hour = sec / 60 / 60

        var str = ""
//        if (day > 0) {
//            str = "${str}${day}天"
//        }
        if (hour > 0) {
            str = "${str}${hour}小时"
        }
        if (minute > 0) {
            str = "${str}${minute}分钟"
        }
        if (second > 0) {
            str = "${str}${second}秒"
        }

        return str
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeMessages(0x001)
    }

    override fun onResume() {
        super.onResume()
        mHandler.removeMessages(0x001)
        tv_rate.text = ""
        SoguApi.getService(application)
                .getType()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            TYPE = this.type as Int //-1=>隐藏入口 0=>未开启  1=>进入评分中心，2=>进入填写页面
                            if (TYPE == 0) {
                                tv_rate.text = "暂未开启"
                            } else if (TYPE == -1) {
                                tr_rate.visibility = View.GONE
                            } else {
                                if (this.time != null) {
                                    mHandler.sendMessageDelayed(mHandler.obtainMessage(0x001, this.time), 1000)
                                }
                            }
                            //
                            XmlDb.open(context).set(Extras.QUANXIAN, "${this.is_see}")
                            XmlDb.open(context).set(Extras.RULE, this.rule_url!!)

                            //
                            XmlDb.open(context).set(Extras.ROLE, "${this.role}")//1=>领导班子 2=>部门负责人 3=>其他员工
                            XmlDb.open(context).set(Extras.ADJUST, "${this.is_adjust}")//领导班子是否可以显示调整项	1显示 0不显示
                        }
                    } //else
                    //showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
    }

    var TYPE = 0   //-1=>隐藏入口 0=>未开启  1=>进入评分中心，2=>进入填写页面
    var role = 0  // 1=>领导班子 2=>部门负责人 3=>其他员工
    var is_adjust = 0// 领导班子是否可以显示调整项	1显示 0不显示

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> {
                doSave();return true
            }
        }
        return false
    }

    private fun compressImage(srcImagePath: String, outWidth: Int, outHeight: Int, maxFileSize: Int): String? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(srcImagePath, options)
        val srcWidth = options.outWidth.toFloat()
        val srcHeight = options.outHeight.toFloat()
        val maxWidth = outWidth.toFloat()
        val maxHeight = outHeight.toFloat()
        val srcRatio = srcWidth / srcHeight
        val outRatio = maxWidth / maxHeight
        var actualOutWidth = srcWidth
        var actualOutHeight = srcHeight

        if (srcWidth > maxWidth || srcHeight > maxHeight) {
            if (srcRatio < outRatio) {
                actualOutHeight = maxHeight
                actualOutWidth = actualOutHeight * srcRatio
            } else if (srcRatio > outRatio) {
                actualOutWidth = maxWidth
                actualOutHeight = actualOutWidth / srcRatio
            } else {
                actualOutWidth = maxWidth
                actualOutHeight = maxHeight
            }
        }
        options.inSampleSize = computSampleSize(options, actualOutWidth, actualOutHeight)
        options.inJustDecodeBounds = false
        var scaledBitmap: Bitmap? = null
        try {
            scaledBitmap = BitmapFactory.decodeFile(srcImagePath, options)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }

        if (scaledBitmap == null) {
            return null//压缩失败
        }
        //生成最终输出的bitmap
        var actualOutBitmap = Bitmap.createScaledBitmap(scaledBitmap, actualOutWidth.toInt(), actualOutHeight.toInt(), true)
        if (actualOutBitmap != scaledBitmap)
            scaledBitmap.recycle()

        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(srcImagePath)
            val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90f)
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180f)
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270f)
            }
            actualOutBitmap = Bitmap.createBitmap(actualOutBitmap, 0, 0,
                    actualOutBitmap.width, actualOutBitmap.height, matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        //进行有损压缩
        val baos = ByteArrayOutputStream()
        var options_ = 100
        actualOutBitmap.compress(Bitmap.CompressFormat.JPEG, options_, baos)//质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)

        var baosLength = baos.toByteArray().size

        while (baosLength / 1024 > maxFileSize) {
            baos.reset()
            options_ = Math.max(0, options_ - 10)
            actualOutBitmap.compress(Bitmap.CompressFormat.JPEG, options_, baos)
            baosLength = baos.toByteArray().size
            if (options_ == 0)
                break
        }
        actualOutBitmap.recycle()

        //将bitmap保存到指定路径
        var fos: FileOutputStream? = null
        val filePath = getOutputFileName(srcImagePath)
        try {
            fos = FileOutputStream(filePath)
            //包装缓冲流,提高写入速度
            val bufferedOutputStream = BufferedOutputStream(fos)
            bufferedOutputStream.write(baos.toByteArray())
            bufferedOutputStream.flush()
        } catch (e: FileNotFoundException) {
            return null
        } catch (e: IOException) {
            return null
        } finally {
            if (baos != null) {
                try {
                    baos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        return filePath
    }

    private fun computSampleSize(options: BitmapFactory.Options, reqWidth: Float, reqHeight: Float): Int {
        val srcWidth = options.outWidth.toFloat()//20
        val srcHeight = options.outHeight.toFloat()//10
        var sampleSize = 1
        if (srcWidth > reqWidth || srcHeight > reqHeight) {
            val withRatio = Math.round(srcWidth / reqWidth)
            val heightRatio = Math.round(srcHeight / reqHeight)
            sampleSize = Math.min(withRatio, heightRatio)
        }
        return sampleSize
    }

    private fun getOutputFileName(srcFilePath: String): String {
        val srcFile = File(srcFilePath)
        val file = File(Environment.getExternalStorageDirectory().path, "imgs")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + File.separator + srcFile.name
    }

    fun doSave() {
        tv_name.text?.trim()?.toString()?.apply {
            user.name = this
        }
        tv_posotion.text?.trim()?.toString()?.apply {
            user.position = this
        }
        tv_email.text?.trim()?.toString()?.apply {
            user.email = this
        }
        tv_note.text?.trim()?.toString()?.apply {
            user.memo = this
        }
        SoguApi.getService(application)
                .saveUser(uid = user.uid!!, name = user.name, phone = user.phone, email = user.email,
                        advice_token = Store.store.getUToken(this),
                        position = user.position, depart_id = user.depart_id, project = user.project, memo = user.memo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("信息保存成功")
                        Store.store.setUser(this@UserEditActivity, user)
                        finish()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("保存失败")
                })
    }


    private fun doUpload(url: String) {
        user.url = url
        Glide.with(this@UserEditActivity)
                .load(url)
                .error(R.drawable.img_logo_user)
                .into(iv_user)
        val imgPath = compressImage(url, 160, 160, 1024 * 1024)
        val file = File(imgPath)
        SoguApi.getService(application)
                .uploadImg(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("uid", user.uid!!.toString())
                        .addFormDataPart("image", file.name, RequestBody.create(MediaType.parse("image/*"), file))
                        .build()).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("头像上传成功")
                        Store.store.setUser(this@UserEditActivity, user)
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("头像上传失败")
                })
    }


    companion object {
        fun start(ctx: Activity?, departList: ArrayList<DepartmentBean>) {
            val intent = Intent(ctx, UserEditActivity::class.java)
            intent.putExtra(Extras.DATA, departList)
            ctx?.startActivity(intent)
        }
    }
}
