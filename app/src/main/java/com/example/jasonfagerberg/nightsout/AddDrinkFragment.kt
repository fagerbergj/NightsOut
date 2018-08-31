package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Spinner

private const val TAG = "AddDrinkFragment"

class AddDrinkFragment : Fragment() {

    private var mFavoritesList: ArrayList<Drink> = ArrayList()
    private lateinit var mFavoritesListAdapter: AddDrinkFragmentFavoritesListAdapter

    private var mRecentsList: ArrayList<Drink> = ArrayList()
    private lateinit var mRecentsListAdapter: AddDrinkFragmentRecentsListAdapter

    private var mFavorited: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // inflate view
        val view = inflater.inflate(R.layout.fragment_add_drink, container, false)

        // main
        val main = (activity as AppCompatActivity)

        //toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_add_drink)
        toolbar.inflateMenu(R.menu.add_drink_menu)
        main.setSupportActionBar(toolbar)
        main.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)

        toolbar.setNavigationIcon(R.drawable.arrow_back_white_24dp)

        toolbar.setNavigationOnClickListener { _: View -> activity!!.onBackPressed() }

        // hide bottom nav bar
        val mainActivity: MainActivity = context as MainActivity
        val botNavBar: BottomNavigationView = mainActivity.findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.INVISIBLE

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        (mainActivity.findViewById(R.id.main_frame) as FrameLayout).layoutParams = params

        // spinner setup
        val dropdown: Spinner = view.findViewById(R.id.spinner_add_drink_amount)
        val items = arrayOf("oz", "beers", "shots", "wine glass")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter

        // recycler view setup
        val favoriteListView: RecyclerView = view.findViewById(R.id.recycler_add_drink_favorites_list)
        val linearLayoutManagerFavorites = LinearLayoutManager(context)
        linearLayoutManagerFavorites.orientation = LinearLayoutManager.HORIZONTAL
        favoriteListView.layoutManager = linearLayoutManagerFavorites

        // recycler view setup
        val recentsListView: RecyclerView = view.findViewById(R.id.recycler_add_drink_recents_list)
        val linearLayoutManagerRecents = LinearLayoutManager(context)
        linearLayoutManagerRecents.orientation = LinearLayoutManager.HORIZONTAL
        recentsListView.layoutManager = linearLayoutManagerRecents

        // todo remove test data
        for (i in 0..9){
            val drink = Drink(ByteArray(0), "This is an Example Drink #" + i.toString(),
                    i*10 + i + i.toDouble()/10, (i*10 + i + i.toDouble()/10), "oz")
            mFavoritesList.add(drink)
        }

        // adapter setup
        mFavoritesListAdapter = AddDrinkFragmentFavoritesListAdapter(context!!, mFavoritesList)
        favoriteListView.adapter = mFavoritesListAdapter

        // adapter setup
        mRecentsListAdapter = AddDrinkFragmentRecentsListAdapter(context!!, mFavoritesList)
        recentsListView.adapter = mRecentsListAdapter

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        fun newInstance(): AddDrinkFragment = AddDrinkFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.add_drink_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        when(resId){
            R.id.btn_toolbar_favorite -> {
                mFavorited = !mFavorited
                if(mFavorited){
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_white_24dp)
                }else{
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_border_white_24dp)
                }
            }
        }

        return true
    }
}
