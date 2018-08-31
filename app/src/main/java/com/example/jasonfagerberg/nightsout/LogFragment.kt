package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import java.util.*
import kotlin.collections.ArrayList


class LogFragment : Fragment() {

    val mLogList : ArrayList<Any> = ArrayList()
    private lateinit var mLogFragmentAdapter: LogFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log, container, false)

        // recycler view
        val logListView: RecyclerView = view.findViewById(R.id.recycler_log)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        logListView.layoutManager = linearLayoutManager

        // toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_log)
        toolbar.inflateMenu(R.menu.empty_menu)

        // todo remove test data
        for (i in 9 downTo 0){
            val session = Session(Date(2018,8,i,0,0,0),
                    i.toDouble(), i.toDouble())
            mLogList.add(session)

            val end = (Math.random()*10).toInt()
            for (x in 0..end){
                val drink = Drink(ByteArray(0), "This is an Example Drink # $x",
                        x*10 + x + x.toDouble()/10, (x*10 + x + x.toDouble()/10), "oz")
                mLogList.add(drink)
            }
        }

        // set adapter
        mLogFragmentAdapter = LogFragmentAdapter(context!!, mLogList)
        logListView.adapter = mLogFragmentAdapter

        // setup bottom nav bar
        val mainActivity: MainActivity = context as MainActivity
        val botNavBar: BottomNavigationView = mainActivity.findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.VISIBLE
        botNavBar.selectedItemId = R.id.bottom_nav_log

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ABOVE, R.id.bottom_navigation_view)
        (mainActivity.findViewById(R.id.main_frame) as FrameLayout).layoutParams = params

        return view
    }

    companion object {
        fun newInstance(): LogFragment = LogFragment()
    }
}
