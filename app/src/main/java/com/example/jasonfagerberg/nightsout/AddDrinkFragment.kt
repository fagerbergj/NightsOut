package com.example.jasonfagerberg.nightsout

import android.graphics.Typeface
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*

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

        // add button setup
        val btnAdd = view.findViewById<MaterialButton>(R.id.btn_add_drink_add)
        btnAdd.setOnClickListener { _ -> addDrink(view)}

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
                    val toast = Toast.makeText(context!!, "Drink Will Be Favorited After Adding", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 450)
                    toast.show()
                }else{
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_border_white_24dp)
                    btnAdd.setText(R.string.text_add)
                    val toast = Toast.makeText(context!!, "Drink Will Not Be Favorited", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 450)
                    toast.show()
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

    private fun addDrink(view: View){
        val editName = view.findViewById<EditText>(R.id.edit_add_drink_name)
        val editAAV = view.findViewById<EditText>(R.id.edit_add_drink_aav)
        val editAmount = view.findViewById<EditText>(R.id.edit_add_drink_amount)

        val textName = view.findViewById<TextView>(R.id.text_add_drink_name)
        val textAAV = view.findViewById<TextView>(R.id.text_add_drink_aav)
        val textAmount = view.findViewById<TextView>(R.id.text_add_drink_amount)

        resetTextView(textName, R.string.text_name)
        resetTextView(textAAV, R.string.text_aav)
        resetTextView(textAmount, R.string.text_amount)

        // make sure string can be parsed to double
        if(!editAAV.text.isEmpty() && "${editAAV.text}"["${editAAV.text}".length-1] == '.'){
            val w = "${editAAV.text}0"
            editAAV.setText(w)
        }
        if(!editAmount.text.isEmpty() && "${editAmount.text}"["${editAmount.text}".length-1] == '.'){
            val w = "${editAmount.text}0"
            editAmount.setText(w)
        }

        // check name isn't empty
        if(editName.text.isEmpty()){
            val toast = Toast.makeText(context!!, "Please Input a Name", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 450)
            toast.show()

            textName.setTypeface(null, Typeface.BOLD)
            textName.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
            return
        } else if (editAAV.text.isEmpty() || ("${editAAV.text}".toDouble() > 100.0)){
            val toast = Toast.makeText(context!!, "Please Input a Valid A.A.V.", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 450)
            toast.show()

            textAAV.setTypeface(null, Typeface.BOLD)
            textAAV.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
            return
        } else if (editAmount.text.isEmpty()){
            val toast = Toast.makeText(context!!, "Please Input a Valid Amount", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 450)
            toast.show()

            textAmount.setTypeface(null, Typeface.BOLD)
            textAmount.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
            return
        }

        editName.text.clear()
        editAAV.text.clear()
        editAmount.text.clear()
        mMainActivity.onBackPressed()
    }

    private fun resetTextView(view: TextView, id: Int){
        view.text = resources.getText(id)
        view.setTypeface(null, Typeface.NORMAL)
        view.setTextColor(ContextCompat.getColor(context!!, R.color.colorText))
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
