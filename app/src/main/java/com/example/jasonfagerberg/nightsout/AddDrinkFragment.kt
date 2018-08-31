package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout

class AddDrinkFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // inflate view
        val view = inflater.inflate(R.layout.fragment_add_drink, container, false)

        //toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbarAddDrink)
        toolbar.inflateMenu(R.menu.empty_menu)
        toolbar.setNavigationIcon(R.drawable.arrow_back_white_24dp)

        toolbar.setNavigationOnClickListener { _: View -> activity!!.onBackPressed() }

        // hide bottom nav bar
        val mainActivity: MainActivity = context as MainActivity
        val botNavBar: BottomNavigationView = mainActivity.findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.INVISIBLE

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        (mainActivity.findViewById(R.id.main_frame) as FrameLayout).layoutParams = params

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        fun newInstance(): AddDrinkFragment = AddDrinkFragment()
    }
}
