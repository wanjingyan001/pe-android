package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import cn.finalteam.rxgalleryfinal.RxGalleryFinal
import cn.finalteam.rxgalleryfinal.imageloader.ImageLoaderType
import cn.finalteam.rxgalleryfinal.rxbus.RxBusResultDisposable
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageRadioResultEvent
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
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
        Store.store.getUser(this)?.apply {
            user = this
            if (!TextUtils.isEmpty(name))
                tv_name?.setText(name)
            if (!TextUtils.isEmpty(phone))
                tv_phone?.setText(phone)
            if (!TextUtils.isEmpty(email))
                tv_email?.setText(email)
            if (!TextUtils.isEmpty(depart_name))
                tv_job?.setText(depart_name)
            if (!TextUtils.isEmpty(url))
                Glide.with(this@UserEditActivity)
                        .load(url)
                        .into(iv_user)
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
//                    .crop()
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
        tv_name.text?.trim()?.apply {
            user.name = this.toString()
        }
        tv_job.text?.trim()?.apply {
            user.position = this.toString()
        }
        tv_phone.text?.trim()?.apply {
            user.phone = this.toString()
        }
        tv_email.text?.trim()?.apply {
            user.email = this.toString()
        }
        SoguApi.getService(application)
                .saveUser(uid = user.uid!!, name = user.name, phone = user.phone, email = user.email, position = user.position)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("信息保存成功")
                        Store.store.setUser(this@UserEditActivity, user)
                        finish()
                    } else
                        showToast(payload.message)
                }, {
                    showToast("保存失败")
                })
    }


    private fun doUpload(url: String) {
        user.url = url
        Glide.with(this@UserEditActivity)
                .load(url)
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
                }, {
                    showToast("头像上传失败")
                })
    }


    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserEditActivity::class.java))
        }
    }
}
