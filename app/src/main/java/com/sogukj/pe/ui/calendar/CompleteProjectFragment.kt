package com.sogukj.pe.ui.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_complete_project.*


/**
 * A simple [Fragment] subclass.
 * Use the [CompleteProjectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CompleteProjectFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_complete_project
    val data = ArrayList<Any>()
    private var companyId: String? = null
    private var mParam2: String? = null
    lateinit var adapter: CompleteAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            companyId = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CompleteAdapter(context, data)
        completeList.layoutManager = LinearLayoutManager(context)
        completeList.adapter = adapter
        companyId?.let { doRequest(it) }
    }


    fun doRequest(id: String) {
        SoguApi.getService(activity.application)
                .projectMatter2(id.toInt(), 2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        data.clear()
                        Log.d("WJY", Gson().toJson(payload.payload))
                        payload.payload?.let {
                            it.forEach {
                                data.add(TodoYear(it.year))
                                it.data.forEach {
                                    data.add(it)
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

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CompleteProjectFragment.
         */
        fun newInstance(param1: String, param2: String): CompleteProjectFragment {
            val fragment = CompleteProjectFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}
