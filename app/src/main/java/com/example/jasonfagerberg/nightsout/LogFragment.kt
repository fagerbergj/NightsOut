package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        return view
    }

    companion object {
        fun newInstance(): LogFragment = LogFragment()
    }
}
