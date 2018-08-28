package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*


/**
 * A simple [Fragment] subclass.
 *
 */
class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"

    // create fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // create new fragment
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }

}
