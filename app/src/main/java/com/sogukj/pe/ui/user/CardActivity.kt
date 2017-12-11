package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import anet.channel.util.Utils.context
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.layout_card_window.*

class CardActivity : Activity() {

    companion object {
        fun start(ctx: Activity?, bean: UserBean) {
            val intent = Intent(ctx, CardActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_card_window)
        val userBean = intent.getSerializableExtra(Extras.DATA) as UserBean
        userBean.let {
            setData(it)
        }
        downloadCard.setOnClickListener {
            if (Utils.saveImage(this, businessCard)) {
                Toast.makeText(this,"名片已经保存至${Environment.getExternalStorageDirectory().absolutePath}",Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this,"权限不足",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    fun setData(bean: UserBean) {
        if (!TextUtils.isEmpty(bean.url)) {
            Glide.with(context)
                    .load(bean.headImage())
                    .into(headerImage)
        }
        cardName.text = bean.name
        cardPosition.text = bean.position
        cardCompanyName.text = bean.depart_name
        cardPhone.text = bean.phone
        cardEmail.text = bean.email
        cardAddress.text = bean.memo
    }
}
