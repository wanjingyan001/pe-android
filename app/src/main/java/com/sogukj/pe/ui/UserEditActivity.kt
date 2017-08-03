package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import cn.finalteam.rxgalleryfinal.RxGalleryFinal
import cn.finalteam.rxgalleryfinal.imageloader.ImageLoaderType
import cn.finalteam.rxgalleryfinal.rxbus.RxBusResultDisposable
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageRadioResultEvent
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.framework.util.Trace
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_edit.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File


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
        setTitle("个人信息")
        val departList = intent.getSerializableExtra(Extras.DATA) as ArrayList<DepartmentBean>?
        Store.store.getUser(this)?.apply {
            user = this
            if (!TextUtils.isEmpty(name))
                tv_name?.setText(name)
            if (!TextUtils.isEmpty(phone))
                tv_phone?.setText(phone)
            if (!TextUtils.isEmpty(email))
                tv_email?.setText(email)
            if (!TextUtils.isEmpty(position))
                tv_posotion?.setText(position)
            if (!TextUtils.isEmpty(depart_name))
                tv_depart?.setText(depart_name)
            if (!TextUtils.isEmpty(url))
                Glide.with(this@UserEditActivity)
                        .load(headImage())
                        .error(R.drawable.img_user_default)
                        .into(iv_user)
        }
        tv_depart.onClick {
            val items = ArrayList<String?>()
            departList?.forEach {
                items.add(it.de_name)
            }
            MaterialDialog.Builder(this@UserEditActivity)
                    .theme(Theme.LIGHT)
                    .title("选择部门")
                    .items(items)
                    .itemsCallbackSingleChoice(-1, object : MaterialDialog.ListCallbackSingleChoice {
                        override fun onSelection(dialog: MaterialDialog?, v: View?, p: Int, s: CharSequence?): Boolean {
                            if (p == -1) return false
                            val data = departList?.get(p)
                            data?.apply {
                                user.depart_id = depart_id
                                user.depart_name = de_name
                            }
                            tv_depart.text = user.depart_name
                            return true
                        }

                    })
                    .positiveText("确定")
                    .negativeText("取消")
                    .show()
        }
        tr_icon.onClick {
            //            val intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(intent, REQ_PHOTO)
            RxGalleryFinal
                    .with(application)
                    .image()
                    .radio()
//                    .cropAspectRatioOptions(0, AspectRatio("1:1", 120f, 120f))
                    .cropMaxResultSize(120, 120)
                    .imageLoader(ImageLoaderType.GLIDE)
                    .subscribe(object : RxBusResultDisposable<ImageRadioResultEvent>() {
                        override fun onEvent(event: ImageRadioResultEvent?) {
                            val path = event?.result?.originalPath
                            if (!TextUtils.isEmpty(path))
                                doUpload(path!!)
                        }
                    })
                    .openGallery();
        }

    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> {
                doSave();return true;
            }
        }
        return false
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
        SoguApi.getService(application)
                .saveUser(uid = user.uid!!, name = user.name, phone = user.phone, email = user.email,
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
                .placeholder(R.drawable.img_user_default)
                .error(R.drawable.img_user_default)
                .into(iv_user)
        val file = File(user.url)
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("uid", user.uid!!.toString())
                .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();
        SoguApi.getService(application)
                .uploadImg(requestBody)
                .observeOn(AndroidSchedulers.mainThread())
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
