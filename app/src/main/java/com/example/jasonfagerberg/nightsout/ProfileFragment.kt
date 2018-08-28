package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        //toolbar setup
        val toolbar: android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbarProfile)
        toolbar.inflateMenu(R.menu.empty_menu)

        return view
    }

    companion object {
        fun newInstance() : ProfileFragment = ProfileFragment()
    }
}
