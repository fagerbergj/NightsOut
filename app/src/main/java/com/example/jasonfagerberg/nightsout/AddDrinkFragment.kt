package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

private const val TAG = "AddDrinkFragment"

class AddDrinkFragment : Fragment() {

    private var mFavoritesList: ArrayList<Drink> = ArrayList()
    private lateinit var mFavoritesListAdapter: AddDrinkFragmentFavoritesListAdapter

    private var mRecentsList: ArrayList<Drink> = ArrayList()
    private lateinit var mRecentsListAdapter: AddDrinkFragmentRecentsListAdapter

    private var mFavorited: Boolean = false

    private lateinit var mMainActivity: MainActivity

    // resulting drink
    lateinit var mResult: Drink

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // inflate view
        val view = inflater.inflate(R.layout.fragment_add_drink, container, false)
        mMainActivity = context as MainActivity

        //toolbar setup
        toolbarSetup(view)

        // hide bottom nav bar
        mMainActivity.hideBottomNavBar()

        // spinner setup
        val dropdown: Spinner = view.findViewById(R.id.spinner_add_drink_amount)
        val items = arrayOf("oz", "beers", "shots", "wine glasses")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter

        // favorites recycler view setup
        val favoriteListView: RecyclerView = view.findViewById(R.id.recycler_add_drink_favorites_list)
        val linearLayoutManagerFavorites = LinearLayoutManager(context)
        linearLayoutManagerFavorites.orientation = LinearLayoutManager.HORIZONTAL
        favoriteListView.layoutManager = linearLayoutManagerFavorites

        // recents recycler view setup
        val recentsListView: RecyclerView = view.findViewById(R.id.recycler_add_drink_recents_list)
        val linearLayoutManagerRecents = LinearLayoutManager(context)
        linearLayoutManagerRecents.orientation = LinearLayoutManager.HORIZONTAL
        recentsListView.layoutManager = linearLayoutManagerRecents

//        // todo remove test data
//        for (i in 0..9){
//            val drink = Drink(ByteArray(0), "This is an Example Drink #" + i.toString(),
//                    i*10 + i + i.toDouble()/10, (i*10 + i + i.toDouble()/10), "oz")
//            mFavoritesList.add(drink)
//        }

        // set empty text views
        mRecentsList.addAll(mFavoritesList)
        showOrHideEmptyTextViews(view)

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

    private fun toolbarSetup(view: View){
        val toolbar:android.support.v7.widget.Toolbar = view.findViewById(R.id.toolbar_add_drink)
        toolbar.inflateMenu(R.menu.add_drink_menu)
        mMainActivity .setSupportActionBar(toolbar)
        mMainActivity .supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)

        toolbar.setNavigationIcon(R.drawable.arrow_back_white_24dp)

        toolbar.setNavigationOnClickListener { _: View -> activity!!.onBackPressed() }
    }

    fun showOrHideEmptyTextViews(view: View){
        val emptyFavorite = view.findViewById<TextView>(R.id.text_favorites_empty_list)
        val emptyRecent = view.findViewById<TextView>(R.id.text_recents_empty_list)

        if(mFavoritesList.isEmpty()){
            emptyFavorite.visibility = View.VISIBLE
        }else{
            emptyFavorite.visibility = View.INVISIBLE
        }

        if(mRecentsList.isEmpty()){
            emptyRecent.visibility = View.VISIBLE
        }else{
            emptyRecent.visibility = View.INVISIBLE
        }
    }
}
