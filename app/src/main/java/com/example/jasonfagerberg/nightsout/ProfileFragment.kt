package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.RelativeLayout


class ProfileFragment : Fragment() {

    private var mFavoritesList: ArrayList<Drink> = ArrayList()
    private lateinit var mFavoritesListAdapter: ProfileFragmentFavoriesListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        //toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_profile)
        toolbar.inflateMenu(R.menu.empty_menu)

        // spinner setup
        val dropdown: Spinner = view.findViewById(R.id.spinner_profile)
        val items = arrayOf("lbs", "kg")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter

        // recycler view setup
        val favoriteListView: RecyclerView = view.findViewById(R.id.recycler_profile_favorites_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        favoriteListView.layoutManager = linearLayoutManager

        // todo remove test data
        for (i in 0..9){
            val drink = Drink(ByteArray(0), "This is an Example Drink #" + i.toString(),
                    i*10 + i + i.toDouble()/10, (i*10 + i + i.toDouble()/10), "oz")
            mFavoritesList.add(drink)
        }

        // adapter setup
        mFavoritesListAdapter = ProfileFragmentFavoriesListAdapter(context!!, mFavoritesList)
        favoriteListView.adapter = mFavoritesListAdapter

        // button setup
        val btnMale: MaterialButton = view.findViewById(R.id.btn_profile_male)
        val btnFemale: MaterialButton = view.findViewById(R.id.btn_profile_female)

        btnMale.setOnClickListener{ _ ->
            btnMale.setEnabled(false)
            btnFemale.setEnabled(true)
        }

        btnFemale.setOnClickListener{ _ ->
            btnFemale.setEnabled(false)
            btnMale.setEnabled(true)
        }

        // setup bottom nav bar
        val mainActivity: MainActivity = context as MainActivity
        val botNavBar: BottomNavigationView = mainActivity.findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.VISIBLE
        botNavBar.selectedItemId = R.id.bottom_nav_profile

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ABOVE, R.id.bottom_navigation_view)
        (mainActivity.findViewById(R.id.main_frame) as FrameLayout).layoutParams = params
        return view
    }

    companion object {
        fun newInstance() : ProfileFragment = ProfileFragment()
    }
}
