package com.wit.jasonfagerberg.nightsout.addDrink

import android.graphics.Typeface
import android.os.Bundle
//import android.util.Log
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.*
import android.widget.*
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.R
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.wit.jasonfagerberg.nightsout.converter.Converter
import com.wit.jasonfagerberg.nightsout.databaseHelper.AddDrinkDatabaseHelper
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import java.util.*

//private const val TAG = "AddDrinkFragment"

class AddDrinkFragment : Fragment() {

    val mConverter = Converter()
    private lateinit var mFavoritesListAdapter: AddDrinkFragmentFavoritesListAdapter
    private lateinit var mRecentsListAdapter: AddDrinkFragmentRecentsListAdapter
    private lateinit var mComplexDrinkMode: AddDrinkFragmentComplexDrink

    private lateinit var mEditName: EditText
    lateinit var mEditAbv: EditText
    lateinit var mEditAmount: EditText
    lateinit var mSpinnerAmount: Spinner

    // booleans that work together to change behavior based on fragment that set this fragment
    var mFavorited: Boolean = false
    private var canUnfavorite = true
    private var complexMode = false

    private lateinit var mMainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        canUnfavorite = true
        // inflate view
        val view = inflater.inflate(R.layout.fragment_add_drink, container, false)
        mMainActivity = context as MainActivity

        //toolbar setup
        toolbarSetup(view)
        drinkNameEditTextSetup(view)

        // hide bottom nav bar
        mMainActivity.hideBottomNavBar()

        // spinner setup
        mSpinnerAmount = view.findViewById(R.id.spinner_add_drink_amount)
        val country = Locale.getDefault().country
        val items = arrayOf("ml", "oz", "beers", "shots", "wine glasses")
        if (country == "US" || country == "LR" || country == "MM") {
            items[0] = "oz"
            items[1] = "ml"
        }
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, items)
        mSpinnerAmount.adapter = adapter

        setupRecentsAndFavoritesRecycler(view)

        // set empty text views
        showOrHideEmptyTextViews(view)

        // add button setup
        val btnAdd = view.findViewById<MaterialButton>(R.id.btn_add_drink_add)
        if (mFavorited) btnAdd.text = resources.getText(R.string.add_favorite)
        btnAdd.setOnClickListener { _ -> addDrink() }

        mEditName = view.findViewById(R.id.edit_add_drink_name)
        mEditAbv = view.findViewById(R.id.edit_add_drink_abv)
        mEditAmount = view.findViewById(R.id.edit_add_drink_amount)

        // Inflate the layout for this fragment
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.add_drink_menu, menu)
        val item = menu!!.findItem(R.id.btn_toolbar_favorite)
        item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_border_white_24dp)
        // if called from profile fragment, the drink will be favorited and cannot be unfavorited
        if (mFavorited) {
            canUnfavorite = false
            item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_white_24dp)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        val btnAdd = view!!.findViewById<MaterialButton>(R.id.btn_add_drink_add)
        when (resId) {
            R.id.btn_toolbar_complex_drink -> {
                complexMode = !complexMode
                if (complexMode) {
                    mComplexDrinkMode = AddDrinkFragmentComplexDrink(this)
                    view!!.findViewById<MaterialButton>(R.id.btn_add_drink_add_alc_source).visibility = View.VISIBLE
                    view!!.findViewById<RecyclerView>(R.id.recycler_add_drink_alcohol_source_list).visibility = View.VISIBLE
                    mMainActivity.showToast("You can now add multiple alcohol sources")
                    item.title = "Simple Drink"
                }else{
                    view!!.findViewById<MaterialButton>(R.id.btn_add_drink_add_alc_source).visibility = View.INVISIBLE
                    view!!.findViewById<RecyclerView>(R.id.recycler_add_drink_alcohol_source_list).visibility = View.INVISIBLE
                    item.title = "Complex Drink"
                }
            }
            R.id.btn_toolbar_favorite -> {
                if (canUnfavorite) mFavorited = !mFavorited
                if (mFavorited) {
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_white_24dp)
                    if (canUnfavorite) btnAdd.setText(R.string.add_and_favorite)
                    mMainActivity.showToast("Drink Will Be Favorited After Adding")
                } else {
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_border_white_24dp)
                    btnAdd.setText(R.string.add)
                    mMainActivity.showToast("Drink Will Not Be Favorited")
                }
            }
            R.id.btn_clear_favorites_list -> {
                if (mMainActivity.mFavoritesList.isEmpty()) return false
                val posAction = {
                    mMainActivity.mDatabaseHelper.deleteRowsInTable("favorites", null)
                    mMainActivity.mFavoritesList.clear()
                    for (drink in mMainActivity.mDrinksList) {
                        drink.favorited = false
                    }
                    showOrHideEmptyTextViews(view!!)
                    mFavoritesListAdapter.notifyDataSetChanged()
                }
                val lightSimpleDialog = LightSimpleDialog(context!!)
                lightSimpleDialog.setActions(posAction, {})
                lightSimpleDialog.show("Are you sure you want to clear all favorites?")
            }
            R.id.btn_clear_recents_list -> {
                if (mMainActivity.mRecentsList.isEmpty()) return false
                val posAction = {
                    mMainActivity.mDatabaseHelper.deleteRowsInTable("drinks", "recent = 1")
                    mMainActivity.mRecentsList.clear()
                    for (drink in mMainActivity.mDrinksList) {
                        drink.recent = false
                    }
                    showOrHideEmptyTextViews(view!!)
                    mRecentsListAdapter.notifyDataSetChanged()
                }
                val lightSimpleDialog = LightSimpleDialog(context!!)
                lightSimpleDialog.setActions(posAction, {})
                lightSimpleDialog.show("Are you sure you want to clear all recent drinks?")
            }
        }
        return true
    }

    private fun toolbarSetup(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_add_drink)
        toolbar.inflateMenu(R.menu.add_drink_menu)
        mMainActivity.setSupportActionBar(toolbar)
        mMainActivity.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)

        toolbar.setNavigationIcon(R.drawable.arrow_back_white_24dp)

        toolbar.setNavigationOnClickListener { _: View -> activity!!.onBackPressed() }
    }

    private fun setupRecentsAndFavoritesRecycler(view: View) {
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

        // adapter setup
        mFavoritesListAdapter = AddDrinkFragmentFavoritesListAdapter(context!!, mMainActivity.mFavoritesList)
        favoriteListView.adapter = mFavoritesListAdapter

        // adapter setup
        mRecentsListAdapter = AddDrinkFragmentRecentsListAdapter(context!!, mMainActivity.mRecentsList)
        recentsListView.adapter = mRecentsListAdapter
    }

    private fun drinkNameEditTextSetup(view: View) {
        val mEditName = view.findViewById<AutoCompleteTextView>(R.id.edit_add_drink_name)
        val addDrinkDBHelper = AddDrinkDatabaseHelper(mMainActivity.mDatabaseHelper)
        val names = addDrinkDBHelper.pullDrinkNames()
        val adapter = ArrayAdapter<String>(context!!, R.layout.fragment_add_drink_name_suggestion_list,
                R.id.text_view_list_item, names)
        mEditName.setAdapter(adapter)

        mEditName.onItemClickListener = AdapterView.OnItemClickListener { _, _, position: Int, _ ->
            val item = adapter.getItem(position)
            val drink = addDrinkDBHelper.getDrinkFromName(item)

            if (drink != null) fillViews(drink.name, drink.abv, drink.amount, drink.measurement)
        }
    }

    fun showOrHideEmptyTextViews(view: View) {
        val emptyFavorite = view.findViewById<TextView>(R.id.text_favorites_empty_list)
        val emptyRecent = view.findViewById<TextView>(R.id.text_recents_empty_list)

        if (mMainActivity.mFavoritesList.isEmpty()) {
            emptyFavorite.visibility = View.VISIBLE
        } else {
            emptyFavorite.visibility = View.INVISIBLE
        }

        if (mMainActivity.mRecentsList.isEmpty()) {
            emptyRecent.visibility = View.VISIBLE
        } else {
            emptyRecent.visibility = View.INVISIBLE
        }
    }

    private fun resetTextView(view: TextView, id: Int) {
        view.text = resources.getText(id)
        view.setTypeface(null, Typeface.NORMAL)
        view.setTextColor(ContextCompat.getColor(context!!, R.color.colorText))
    }

    fun fillViews(name: String, abv: Double, amount: Double, measurement: String) {
        val mEditName = view!!.findViewById<EditText>(R.id.edit_add_drink_name)
        val mEditAbv = view!!.findViewById<EditText>(R.id.edit_add_drink_abv)
        val mEditAmount = view!!.findViewById<EditText>(R.id.edit_add_drink_amount)
        val dropdown = view!!.findViewById<Spinner>(R.id.spinner_add_drink_amount)

        mEditName.setText(name)
        mEditAbv.setText(abv.toString())
        mEditAmount.setText(amount.toString())
        val country = Locale.getDefault().country
        val items = arrayOf("ml", "oz", "beers", "shots", "wine glasses")
        if (country == "US" || country == "LR" || country == "MM") {
            items[0] = "oz"
            items[1] = "ml"
        }
        dropdown.setSelection(items.indexOf(measurement))
    }

    private fun addDrink() {
        if (isInputErrors() && !complexMode) return
        if (complexMode && mComplexDrinkMode.listIsEmpty() && isInputErrors()) return
        if (complexMode) mComplexDrinkMode.addToAlcoholSourceList()

        val name = mEditName.text.toString()
        val abv = if (!complexMode) mEditAbv.text.toString().toDouble() else mComplexDrinkMode.weightedAverageAbv()
        val amount = if (!complexMode) mEditAmount.text.toString().toDouble() else mComplexDrinkMode.sumAmount()
        val measurement = if (!complexMode) mSpinnerAmount.selectedItem.toString() else "oz"
        val dbTalker = AddDrinkFragmentDatabaseTalker(this, mMainActivity, canUnfavorite, mFavorited)
        dbTalker.buildDrinkAndAddToList(name, abv, amount, measurement)

        mEditName.text.clear()
        mEditAbv.text.clear()
        mEditAmount.text.clear()
        complexMode = false
        mMainActivity.onBackPressed()
    }

    fun addToDrinkList(drink: Drink) {
        mMainActivity.mDrinksList.add(drink)

        if (mMainActivity.mRecentsList.contains(drink)) {
            mMainActivity.mRecentsList[mMainActivity.mRecentsList.indexOf(drink)].recent = false
            mMainActivity.mRecentsList.remove(drink)
        }
        mMainActivity.mRecentsList.add(0, drink)

        if (mMainActivity.mRecentsList.size > 25) {
            mMainActivity.mRecentsList[mMainActivity.mRecentsList.size - 1].recent = false
            mMainActivity.mRecentsList.removeAt(mMainActivity.mRecentsList.size - 1)
        }

        if (drink.favorited) {
            addToFavoritesList(drink)
        }
    }

    fun addToFavoritesList(drink: Drink) {
        mMainActivity.mFavoritesList.remove(drink)
        mMainActivity.mFavoritesList.add(0, drink)
    }

    fun isInputErrors(): Boolean {
        val textName = view!!.findViewById<TextView>(R.id.text_add_drink_name)
        val textABV = view!!.findViewById<TextView>(R.id.text_add_drink_abv)
        val textAmount = view!!.findViewById<TextView>(R.id.text_add_drink_amount)

        resetTextView(textName, R.string.name)
        resetTextView(textABV, R.string.abv)
        resetTextView(textAmount, R.string.amount)

        val abv = mConverter.stringToDouble(mEditAbv.text.toString())
        val amount = mConverter.stringToDouble(mEditAmount.text.toString())

        if (!abv.isNaN()) mEditAbv.setText(abv.toString())
        if (!amount.isNaN()) mEditAmount.setText(amount.toString())

        var inputError = false
        var message = " "

        if (amount.isNaN()) {
            message = ", amount$message"
            setTextViewToRedAndBold(textAmount)
            inputError = true
        }

        if (abv.isNaN() || (abv > 100.0)) {
            message = ", abv$message"
            setTextViewToRedAndBold(textABV)
            inputError = true
        }

        if (mEditName.text.isEmpty()) {
            message = ", name$message"
            setTextViewToRedAndBold(textName)
            inputError = true
        }
        if (inputError) mMainActivity.showToast("Please enter a valid ${message.substring(2, message.length)}")
        return inputError
    }

    private fun setTextViewToRedAndBold(text: TextView) {
        text.setTypeface(null, Typeface.BOLD)
        text.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
    }
}
