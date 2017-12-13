package com.sogukj.pe.ui.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.CompoundButton
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_todo.*


/**
 * A simple [Fragment] subclass.
 * Use the [TodoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodoFragment : BaseFragment(), ScheduleItemClickListener {


    override val containerViewId: Int
        get() = R.layout.fragment_todo
    val data = ArrayList<Any>()
    lateinit var adapter: TodoAdapter
    private var companyId: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            companyId = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TodoAdapter(data, context)
        adapter.setListener(this)
        todoList.layoutManager = LinearLayoutManager(context)
        todoList.adapter = adapter
        companyId?.let { doRequest(it) }
    }

    fun doRequest(id: String) {
        SoguApi.getService(activity.application)
                .projectMatter2(id.toInt(), 3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        data.clear()
                        payload.payload?.let {
                            val yearList = ArrayList<String>()
                            val dayList = ArrayList<String>()
                            val infoList = ArrayList<KeyNode>()
                            it.forEachIndexed { index, matterDetails ->
                                yearList.add(matterDetails.year)
                                matterDetails.data.forEach {
                                    dayList.add(it.end_time?.split(" ")?.get(0)!!)
                                    infoList.add(it)
                                }
                            }
                            yearList.forEach {
                                data.add(TodoYear(it))
                                dayList.forEachIndexed { _, s ->
                                    if (s.substring(0, 4) == it) {
                                        data.add(TodoDay(s))
                                    }
                                    infoList.forEachIndexed { _, keyNode ->
                                        if (keyNode.end_time?.split(" ")?.get(0).equals(s)) {
                                            data.add(keyNode)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        showToast(payload.message)
                    }
                }, { e -> Trace.e(e) }, {
                    adapter.notifyDataSetChanged()
                })
    }

    override fun onItemClick(view: View, position: Int) {

    }

    override fun finishCheck(buttonView: CompoundButton, isChecked: Boolean, position: Int) {
        val keyNode = data[position] as KeyNode
        keyNode.data_id?.let { finishTask(it) }
    }


    fun finishTask(id: Int) {
        SoguApi.getService(activity.application)
                .finishTask(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        companyId?.let { doRequest(it) }
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TodoFragment.
         */
        fun newInstance(param1: String, param2: String): TodoFragment {
            val fragment = TodoFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
