package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.IM.TeamSelectActivity
import com.sogukj.pe.ui.main.ContactsActivity
import com.sogukj.pe.util.MyGlideUrl
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import kotlinx.android.synthetic.main.activity_arrange_person.*
import org.jetbrains.anko.find

class ArrangePersonActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_confirm
    lateinit var alreadyList: ArrayList<UserBean>
    lateinit var adapter: RecyclerAdapter<UserBean>

    companion object {
        fun start(context: Activity, alreadyList: ArrayList<UserBean>, requestCode: Int? = null, position: Int) {
            val intent = Intent(context, ArrangePersonActivity::class.java)
            intent.putExtra(Extras.DATA, alreadyList)
            intent.putExtra(Extras.ID, position)
            val code = requestCode ?: Extras.REQUESTCODE
            context.startActivityForResult(intent, code)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrange_person)
        title = "选择参加人"
        setBack(true)
        alreadyList = intent.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            AlreadyHolder(_adapter.getView(R.layout.item_team_organization_chlid, parent))
        })
        alreadyList.forEachIndexed { index, userBean ->
            adapter.selectedItems.add(index)
        }
        if (alreadyList.isNotEmpty()) {
            selectAllLayout.visibility = View.VISIBLE
            selectAll.isSelected = true
        } else {
            selectAllLayout.visibility = View.INVISIBLE
        }
        adapter.dataList.addAll(alreadyList)
        resultList.layoutManager = LinearLayoutManager(this)
        resultList.adapter = adapter

        toContacts.setOnClickListener {
//            TeamSelectActivity.startForResult(this,
//                    true, alreadyList, false,
//                    false, true, Extras.REQUESTCODE)
            ContactsActivity.start(this,alreadyList,true,false,Extras.REQUESTCODE)
        }
        selectAllLayout.setOnClickListener {
            selectAll.isSelected = !selectAll.isSelected
            alreadyList.forEachIndexed { index, userBean ->
                if (selectAll.isSelected) {
                    if (!adapter.selectedItems.contains(index)) {
                        adapter.selectedItems.add(index)
                    }
                } else {
                    if (adapter.selectedItems.contains(index)) {
                        adapter.selectedItems.remove(index)
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_confirm -> {
                val intent = Intent()
                if (adapter.selectedItems.isEmpty()) {
                    alreadyList.clear()
                }
                intent.putExtra(Extras.DATA, alreadyList)
                intent.putExtra(Extras.ID, getIntent().getIntExtra(Extras.ID, 0))
                setResult(Extras.RESULTCODE, intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE) {
            alreadyList = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            alreadyList.forEachIndexed { index, userBean ->
                adapter.selectedItems.add(index)
            }
            selectAll.isSelected = true
            adapter.dataList.clear()
            adapter.dataList.addAll(alreadyList)
            adapter.notifyDataSetChanged()
            if (alreadyList.isNotEmpty()) {
                selectAllLayout.visibility = View.VISIBLE
                selectAll.isSelected = true
            } else {
                selectAllLayout.visibility = View.INVISIBLE
            }
        }
    }


    inner class AlreadyHolder(convertView: View) : RecyclerHolder<UserBean>(convertView) {
        val selectIcon = convertView.find<ImageView>(R.id.selectIcon)
        val userImg = convertView.find<CircleImageView>(R.id.userHeadImg)
        val userName = convertView.find<TextView>(R.id.userName)
        val userPosition = convertView.find<TextView>(R.id.userPosition)
        override fun setData(view: View, data: UserBean, position: Int) {
            selectIcon.visibility = View.VISIBLE
            selectIcon.isSelected = adapter.isSelected(position)
            if (data.url.isNullOrEmpty()) {
                val ch = data.name?.first()
                userImg.setChar(ch)
            } else {
                Glide.with(context)
                        .load(MyGlideUrl(data.url))
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(userImg)
            }
//            Glide.with(this@ArrangePersonActivity)
//                    .load(data.url)
//                    .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
//                    .into(userImg)
            userName.text = data.name
            userPosition.text = data.position
            convertView.setOnClickListener {
                selectIcon.isSelected = !selectIcon.isSelected
                if (alreadyList.contains(data) && !selectIcon.isSelected && adapter.isSelected(position)) {
                    alreadyList.remove(data)
                    adapter.selectedItems.remove(position)
                }
                if (!alreadyList.contains(data) && selectIcon.isSelected && !adapter.isSelected(position)) {
                    alreadyList.add(data)
                    adapter.selectedItems.add(position)
                }
                selectAll.isSelected = adapter.selectedItems.size == adapter.itemCount
            }
        }
    }
}
