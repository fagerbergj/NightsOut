package com.example.jasonfagerberg.nightsout.addDrink

import android.graphics.Typeface
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.*
import com.example.jasonfagerberg.nightsout.main.Drink
import com.example.jasonfagerberg.nightsout.R
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.AdapterView
import com.example.jasonfagerberg.nightsout.main.MainActivity
import java.util.*

private const val TAG = "AddDrinkFragment"

class AddDrinkFragment : Fragment() {

    private lateinit var mFavoritesListAdapter: AddDrinkFragmentFavoritesListAdapter
    private lateinit var mRecentsListAdapter: AddDrinkFragmentRecentsListAdapter

    // booleans that work together to change behavior based on fragment that set this fragment
    var mFavorited: Boolean = false
    private var canUnfavorite = true

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
        val dropdown: Spinner = view.findViewById(R.id.spinner_add_drink_amount)
        val country = Locale.getDefault().country
        val items = arrayOf("ml", "oz", "beers", "shots", "wine glasses")
        if (country == "US" || country == "LR" || country == "MM"){
            items[0] = "oz"
            items[1] = "ml"
        }
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
        mRecentsListAdapter = AddDrinkFragmentRecentsListAdapter(context!!, mMainActivity.mRecentsList)
        recentsListView.adapter = mRecentsListAdapter

        // add button setup
        val btnAdd = view.findViewById<MaterialButton>(R.id.btn_add_drink_add)
        if (mFavorited) btnAdd.text = resources.getText(R.string.add_favorite)
        btnAdd.setOnClickListener { _ -> addDrink(view)}

        // Inflate the layout for this fragment
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.add_drink_menu, menu)
        val item = menu!!.findItem(R.id.btn_toolbar_favorite)
        item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_border_white_24dp)
        // if called from profile fragment, the drink will be favorited and cannot be unfavorited
        if (mFavorited){
            canUnfavorite = false
            item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_white_24dp)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        val btnAdd = view!!.findViewById<MaterialButton>(R.id.btn_add_drink_add)
        when (resId) {
            R.id.btn_toolbar_favorite -> {
                if (canUnfavorite) mFavorited = !mFavorited
                if (mFavorited) {
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_white_24dp)
                    if(canUnfavorite) btnAdd.setText(R.string.add_and_favorite)
                    showToast("Drink Will Be Favorited After Adding")
                } else {
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.favorite_border_white_24dp)
                    btnAdd.setText(R.string.add)
                    showToast("Drink Will Not Be Favorited")
                }
            }
            R.id.btn_clear_favorites_list -> {
                mMainActivity.mDatabaseHelper.deleteAllFavorites()
                mMainActivity.mFavoritesList.clear()
                for (drink in mMainActivity.mDrinksList){
                    drink.favorited = false
                }
                showOrHideEmptyTextViews(view!!)
                mFavoritesListAdapter.notifyDataSetChanged()
            }
            R.id.btn_clear_recents_list ->{
                mMainActivity.mDatabaseHelper.deleteAllRecents()
                mMainActivity.mRecentsList.clear()
                for (drink in mMainActivity.mDrinksList){
                    drink.recent = false
                }
                showOrHideEmptyTextViews(view!!)
                mRecentsListAdapter.notifyDataSetChanged()
            }
        }
        return true
    }

    private fun toolbarSetup(view: View){
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_add_drink)
        toolbar.inflateMenu(R.menu.add_drink_menu)
        mMainActivity .setSupportActionBar(toolbar)
        mMainActivity .supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)

        toolbar.setNavigationIcon(R.drawable.arrow_back_white_24dp)

        toolbar.setNavigationOnClickListener { _: View -> activity!!.onBackPressed() }
    }

    private fun drinkNameEditTextSetup(view: View){
        val editName = view.findViewById<AutoCompleteTextView>(R.id.edit_add_drink_name)
        val names = mMainActivity.mDatabaseHelper.pullDrinkNames()
        val adapter = ArrayAdapter<String>(context!!, R.layout.fragment_add_drink_name_suggestion_list,
                R.id.text_view_list_item, names)
        editName.setAdapter(adapter)

        editName.onItemClickListener = AdapterView.OnItemClickListener { _, _, position: Int, _ ->
            val item = adapter.getItem(position)
            val drink = mMainActivity.mDatabaseHelper.getDrinkFromName(item!!)
            fillViews(drink.name, drink.abv, drink.amount, drink.measurement)
        }
    }

    private fun addDrink(view: View){
        val editName = view.findViewById<EditText>(R.id.edit_add_drink_name)
        val editABV = view.findViewById<EditText>(R.id.edit_add_drink_abv)
        val editAmount = view.findViewById<EditText>(R.id.edit_add_drink_amount)

        // if user input invalid shit, return
        if(isInputErrors(editName, editABV, editAmount)) return

        val name = editName.text.toString()
        val abv = editABV.text.toString().toDouble()
        val amount = editAmount.text.toString().toDouble()
        val measurement = view.findViewById<Spinner>(R.id.spinner_add_drink_amount).selectedItem.toString()
        buildDrinkAndAddToList(name, abv, amount, measurement)

        editName.text.clear()
        editABV.text.clear()
        editAmount.text.clear()
        mMainActivity.onBackPressed()
    }

    private fun isInputErrors(editName: EditText, editABV: EditText, editAmount: EditText):Boolean{
        val textName = view!!.findViewById<TextView>(R.id.text_add_drink_name)
        val textABV = view!!.findViewById<TextView>(R.id.text_add_drink_abv)
        val textAmount = view!!.findViewById<TextView>(R.id.text_add_drink_amount)

        resetTextView(textName, R.string.name)
        resetTextView(textABV, R.string.abv)
        resetTextView(textAmount, R.string.amount)

        // make sure string can be parsed to double
        if(!editABV.text.isEmpty() && "${editABV.text}"["${editABV.text}".length-1] == '.'){
            val w = "${editABV.text}0"
            editABV.setText(w)
        }
        if(!editAmount.text.isEmpty() && "${editAmount.text}"["${editAmount.text}".length-1] == '.'){
            val w = "${editAmount.text}0"
            editAmount.setText(w)
        }

        // check name isn't empty
        if(editName.text.isEmpty()){
            showToast("Please Input a Name")
            setTextViewToRedAndBold(textName)
            return true
        } else if (editABV.text.isEmpty() || ("${editABV.text}".toDouble() > 100.0)){
            showToast("Please Input a Valid A.A.V.")
            setTextViewToRedAndBold(textABV)
            return true
        } else if (editAmount.text.isEmpty()){
            showToast("Please Input a Valid Amount")
            setTextViewToRedAndBold(textAmount)
            return true
        }
        return false
    }

    private fun buildDrinkAndAddToList(name: String, abv: Double, amount: Double, measurement: String){
        val drink = Drink(-1, name, abv, amount, measurement, mFavorited, true, mMainActivity.getTimeNow())

        setDrinkId(drink)
        setDrinkFavorited(drink)

        if (canUnfavorite) addToDrinkList(drink)
        else addToFavoritesList(drink)
    }

    private fun setDrinkId(drink: Drink){
        val foundID = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        val existsInDB = foundID != -1

        if(!existsInDB){
            mMainActivity.mDatabaseHelper.insertDrinkIntoDrinksTable(drink)
            drink.id = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        }else{
            drink.id = foundID
        }
    }

    private fun setDrinkFavorited(drink: Drink){
        drink.favorited = mMainActivity.mFavoritesList.contains(drink)
        Log.v(TAG, "Favorited from db ${drink.favorited}")

        if(mMainActivity.mDrinksList.contains(drink)){
            val index = mMainActivity.mDrinksList.indexOf(drink)
            val drinkInSession = mMainActivity.mDrinksList[index]
            drink.favorited = drinkInSession.favorited
            Log.v(TAG, "Favorited from current session ${drink.favorited}")
        }

        if(mFavorited){
            drink.favorited = true
            Log.v(TAG, "Favorited from top bar ${drink.favorited}")
            for (d in mMainActivity.mDrinksList){
                if (d == drink){
                    d.favorited = true
                }
            }
        }
    }

    private fun setTextViewToRedAndBold(text: TextView){
        text.setTypeface(null, Typeface.BOLD)
        text.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
    }

    private fun showToast(message: String){
        val toast = Toast.makeText(context!!, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 450)
        toast.show()
    }

    private fun addToDrinkList(drink: Drink){
        mMainActivity.mDrinksList.add(drink)

        if(mMainActivity.mRecentsList.contains(drink)){
            mMainActivity.mRecentsList[mMainActivity.mRecentsList.indexOf(drink)].recent = false
            mMainActivity.mRecentsList.remove(drink)
        }
        mMainActivity.mRecentsList.add(0, drink)

        if (mMainActivity.mRecentsList.size > 25){
            mMainActivity.mRecentsList[mMainActivity.mRecentsList.size - 1].recent = false
            mMainActivity.mRecentsList.removeAt(mMainActivity.mRecentsList.size - 1)
        }

        if(drink.favorited){
            addToFavoritesList(drink)
        }
    }

    private fun addToFavoritesList(drink: Drink){
        mMainActivity.mFavoritesList.remove(drink)
        mMainActivity.mFavoritesList.add(0, drink)
    }

    fun fillViews(name: String, abv: Double, amount: Double, measurement: String){
        val editName = view!!.findViewById<EditText>(R.id.edit_add_drink_name)
        val editABV = view!!.findViewById<EditText>(R.id.edit_add_drink_abv)
        val editAmount = view!!.findViewById<EditText>(R.id.edit_add_drink_amount)
        val dropdown = view!!.findViewById<Spinner>(R.id.spinner_add_drink_amount)

        editName.setText(name)
        editABV.setText(abv.toString())
        editAmount.setText(amount.toString())
        val country = Locale.getDefault().country
        val items = arrayOf("ml", "oz", "beers", "shots", "wine glasses")
        if (country == "US" || country == "LR" || country == "MM"){
            items[0] = "oz"
            items[1] = "ml"
        }
        dropdown.setSelection(items.indexOf(measurement))
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
