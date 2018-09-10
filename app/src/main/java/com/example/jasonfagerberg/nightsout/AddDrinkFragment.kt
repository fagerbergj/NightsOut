package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.design.button.MaterialButton
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

    private lateinit var mFavoritesListAdapter: AddDrinkFragmentFavoritesListAdapter
    private lateinit var mRecentsListAdapter: AddDrinkFragmentRecentsListAdapter

    private var mFavorited: Boolean = false

    private lateinit var mMainActivity: MainActivity

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

        // set empty text views
        showOrHideEmptyTextViews(view)

        // adapter setup
        mFavoritesListAdapter = AddDrinkFragmentFavoritesListAdapter(context!!, mMainActivity.mFavoritesList)
        favoriteListView.adapter = mFavoritesListAdapter

        // adapter setup
        mRecentsListAdapter = AddDrinkFragmentRecentsListAdapter(context!!, mMainActivity.mFavoritesList)
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
        val btnAdd = view!!.findViewById<MaterialButton>(R.id.btn_add_drink_add)
        when(resId){
            R.id.btn_toolbar_favorite -> {
                mFavorited = !mFavorited
                if(mFavorited){
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_white_24dp)
                    btnAdd.setText(R.string.text_add_and_favorite)
                }else{
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_border_white_24dp)
                    btnAdd.setText(R.string.text_add)
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

        if(mMainActivity.mFavoritesList.isEmpty()){
            emptyFavorite.visibility = View.VISIBLE
        }else{
            emptyFavorite.visibility = View.INVISIBLE
        }

        if(mMainActivity.mRecentsList.isEmpty()){
            emptyRecent.visibility = View.VISIBLE
        }else{
            emptyRecent.visibility = View.INVISIBLE
        }
    }
}
