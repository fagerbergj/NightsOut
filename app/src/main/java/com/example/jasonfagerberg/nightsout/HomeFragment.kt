package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"

    // create fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // inflate layout
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbarHome)
        toolbar.inflateMenu(R.menu.home_menu)

        // return
        return view
    }

    // create new fragment
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }
}
