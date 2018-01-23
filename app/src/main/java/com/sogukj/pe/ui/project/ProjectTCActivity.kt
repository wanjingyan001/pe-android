package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_tc.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

class ProjectTCActivity : ToolbarActivity() {

    companion object {
        // isView  true表示历史记录，false表示要退出的项目
        fun start(ctx: Activity?, isView: Boolean, bean: ProjectBean) {
            val intent = Intent(ctx, ProjectTCActivity::class.java)
            intent.putExtra(Extras.TYPE, isView)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivityForResult(intent, 0x001)
        }
    }

    var type = false
    lateinit var project: ProjectBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_tc)
        setBack(true)
        title = "退出"

        type = intent.getBooleanExtra(Extras.TYPE, false)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        titleComp.text = project.name
        bianhao.text = "项目编号${project.number}"

        if (type) {
            ll_btn.visibility = View.GONE
        } else {
            part_quit.backgroundResource = R.drawable.tc_true
            part_quit.textColor = Color.parseColor("#1787fb")
            total_quit.backgroundResource = R.drawable.tc_false
            total_quit.textColor = Color.parseColor("#282828")

            part_blue.visibility = View.INVISIBLE
            tr_wtccb.visibility = View.GONE
            tr_bc.visibility = View.GONE
            tr_tzzt.visibility = View.VISIBLE
            tr_bck.visibility = View.VISIBLE
            mType = 1

            part_quit.setOnClickListener {
                part_quit.backgroundResource = R.drawable.tc_true
                part_quit.textColor = Color.parseColor("#1787fb")
                total_quit.backgroundResource = R.drawable.tc_false
                total_quit.textColor = Color.parseColor("#282828")

                tr_wtccb.visibility = View.GONE
                tr_bc.visibility = View.GONE
                tr_tzzt.visibility = View.VISIBLE
                tr_bck.visibility = View.VISIBLE

                mType = 1
            }

            total_quit.setOnClickListener {
                total_quit.backgroundResource = R.drawable.tc_true
                total_quit.textColor = Color.parseColor("#1787fb")
                part_quit.backgroundResource = R.drawable.tc_false
                part_quit.textColor = Color.parseColor("#282828")

                tr_wtccb.visibility = View.VISIBLE
                tr_bc.visibility = View.VISIBLE
                tr_tzzt.visibility = View.GONE
                tr_bck.visibility = View.GONE

                mType = 2
            }

            btn_commit.setOnClickListener {
                upload()
            }
        }
    }

    var mType = 1

    fun upload() {
        var map = HashMap<String, Any>()
        var content = HashMap<String, Any>()

        if (mType == 1) {
            content.put("company_id", project.company_id!!)
            content.put("type", mType)
        } else if (mType == 2) {
            content.put("company_id", project.company_id!!)
            content.put("type", mType)
        }

        map.put("ae", content)

        //company_id	number		公司ID	非空
        //type	number		类型	非空（1=>部分退出，2=>全部退出）

        //invest	string		投资主体	type=2时非空，type=1时隐藏此字段
        //cost	string		成本	非空
        //income	string		退出收入	非空
        //compensation	string		补偿款	type=2时可空，type=1时隐藏此字段
        //profit	string		分红	非空
        //outIncome	string		退出收益	非空
        //investRate	string		投资收益率	非空
        //investTime	string		投资时间	非空(格式如2018-01-16)
        //outTime	string		退出时间	非空(格式如2018-01-16)
        //days	number		投资天数	非空
        //annualRate	string		年化收益率	非空
        //investHour	string		投资时长	非空
        //IRR	string		IRR	非空
        //supply	string		补充	type=1时可空，type=2隐藏此字段
        //surplus	string		未退出成本	type=1时非空，type=2隐藏此字段
        //summary	string		退出总结	可空

        SoguApi.getService(application)
                .addQuit(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
    }
}
