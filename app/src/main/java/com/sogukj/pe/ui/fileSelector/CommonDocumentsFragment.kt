package com.sogukj.pe.ui.fileSelector

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sogukj.pe.R
import kotlinx.android.synthetic.main.fragment_common_documents.*

class CommonDocumentsFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    val titles = listOf("本应用", "微信", "QQ", "钉钉","全部")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_common_documents, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragments = listOf<Fragment>(
                DocumentsFragment.newInstance(DocumentsFragment.PE_LOACL),
                DocumentsFragment.newInstance(DocumentsFragment.WX_DOC),
                DocumentsFragment.newInstance(DocumentsFragment.QQ_DOC),
                DocumentsFragment.newInstance(DocumentsFragment.DING_TALK),
                DocumentsFragment.newInstance(DocumentsFragment.ALL_DOC))
        documentList.adapter = DocPageAdapter(childFragmentManager, fragments)
        tab.setupWithViewPager(documentList)
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String? = null, param2: String? = null): CommonDocumentsFragment {
            val fragment = CommonDocumentsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    inner class DocPageAdapter(fm: FragmentManager, val fragments: List<Fragment>) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }
}
