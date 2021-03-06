package com.wit.jasonfagerberg.nightsout.addDrink

// import android.util.Log
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.drinkSuggestion.DrinkSuggestionArrayAdapter
import com.wit.jasonfagerberg.nightsout.addDrink.drinkSuggestion.DrinkSuggestionAutoCompleteView
import com.wit.jasonfagerberg.nightsout.utils.Converter
import com.wit.jasonfagerberg.nightsout.databaseHelper.AddDrinkDatabaseHelper
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.main.NightsOutActivity
import com.wit.jasonfagerberg.nightsout.manageDB.ManageDBActivity
import java.util.*
import kotlin.collections.ArrayList

// private const val TAG = "AddDrinkActivity"

class AddDrinkActivity : NightsOutActivity() {
    val mConverter = Converter()
    private lateinit var mFavoritesListAdapter: AddDrinkActivityFavoritesListAdapter
    private lateinit var mRecentsListAdapter: AddDrinkActivityRecentsListAdapter
    private lateinit var mComplexDrinkHelper: ComplexDrinkHelper

    private lateinit var mEditName: EditText
    lateinit var mEditAbv: EditText
    lateinit var mEditAmount: EditText
    lateinit var mSpinnerAmount: Spinner

    // booleans that work together to change behavior based on fragment that set this fragment
    private var mFavorited: Boolean = false
    private var canUnfavorite = true
    private var complexMode = false

    lateinit var mRecentsList: ArrayList<Drink>
    lateinit var mFavoritesList: ArrayList<Drink>

    lateinit var autoCompleteView: DrinkSuggestionAutoCompleteView

    lateinit var mDatabaseHelper: AddDrinkDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_drink)

        toolbarSetup()
        drinkNameEditTextSetup()
        setupSpinner()
        setupRecentsAndFavoritesRecycler()

        canUnfavorite = intent.getBooleanExtra("CAN_UNFAVORITE", true)
        mFavorited = intent.getBooleanExtra("FAVORITED", false)

        if (savedInstanceState != null) {
            complexMode = savedInstanceState.getBoolean("complexMode")
            mFavorited = savedInstanceState.getBoolean("mFavorited")
            mComplexDrinkHelper = ComplexDrinkHelper(this)
            val sourceAbv = savedInstanceState.getDoubleArray("sourceAbv")!!
            val sourceAmount = savedInstanceState.getDoubleArray("sourceAmount")!!
            val sourceMeasurement = savedInstanceState.getStringArrayList("sourceMeasure") !!
            mComplexDrinkHelper.rebuildAlcSourceList(sourceAbv, sourceAmount, sourceMeasurement)
        } else if (!complexMode) {
            // if onCreate is being called for the first time, create a new complex drink helper
            // in case that button is pressed
            mComplexDrinkHelper = ComplexDrinkHelper(this)
        }

        setupComplexModeCheckbox()
        mDatabaseHelper = AddDrinkDatabaseHelper(this, Constants.DB_NAME, null, Constants.DB_VERSION)
    }

    override fun onStart() {
        super.onStart()
        mDatabaseHelper.openDatabase()
        initData()
        showOrHideEmptyTextViews()
        setupAddButton()
    }

    override fun onStop() {
        mRecentsList.clear()
        mFavoritesList.clear()
        mDatabaseHelper.closeDatabase()
        super.onStop()
    }

    private fun initData() {
        mFavoritesList = mDatabaseHelper.pullFavoriteDrinks()
        mRecentsList = mDatabaseHelper.pullRecentDrinks()
    }

    private fun setupSpinner() {
        mSpinnerAmount = findViewById(R.id.spinner_add_drink_amount)
        mSpinnerAmount.setSelection(2)
        val country = Locale.getDefault().country
        val items = Constants.VOLUME_MEASUREMENTS_METRIC_FIRST
        if (country == "US" || country == "LR" || country == "MM") {
            items[0] = "oz"
            items[1] = "ml"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        mSpinnerAmount.adapter = adapter
    }

    private fun setupAddButton() {
        val btnAdd = findViewById<MaterialButton>(R.id.btn_add_drink_add)
        // change button color based off mFavorited
        if (mFavorited) {
            setButtonColor(btnAdd, R.color.colorLightRed)
            btnAdd.text = resources.getText(R.string.add_favorite)
        } else {
            setButtonColor(btnAdd, R.color.colorGreen)
        }
        btnAdd.setOnClickListener { addDrink() }
    }

    private fun setupComplexModeCheckbox() {
        val chkComplex = findViewById<CheckBox>(R.id.chkBox_complexDrink)
        chkComplex.isChecked = complexMode
        setComplexDrinkViews()
        chkComplex.setOnClickListener {
            complexMode = chkComplex.isChecked
            setComplexDrinkViews()
        }

        mEditName = findViewById(R.id.auto_drink_suggestion)
        mEditAbv = findViewById(R.id.edit_add_drink_abv)
        mEditAmount = findViewById(R.id.edit_add_drink_amount)
    }

    private fun setComplexDrinkViews() {
        // if complex drinks can me added, make sure the appropriate views are visible
        if (complexMode) {
            mComplexDrinkHelper.findViews(this)
            findViewById<MaterialButton>(R.id.btn_add_drink_add_alc_source).visibility = View.VISIBLE
            findViewById<RecyclerView>(R.id.recycler_add_drink_alcohol_source_list).visibility = View.VISIBLE
            showToast("You can now add multiple alcohol sources")
        } else {
            findViewById<MaterialButton>(R.id.btn_add_drink_add_alc_source).visibility = View.INVISIBLE
            findViewById<RecyclerView>(R.id.recycler_add_drink_alcohol_source_list).visibility = View.INVISIBLE
            findViewById<RecyclerView>(R.id.recycler_add_drink_alcohol_source_list).adapter = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("complexMode", complexMode)
        outState.putBoolean("mFavorited", mFavorited)
        val sourceAbv = DoubleArray(mComplexDrinkHelper.listAlcoholSources.size)
        val sourceAmount = DoubleArray(mComplexDrinkHelper.listAlcoholSources.size)
        val sourceMeasure = ArrayList<String>()
        for (i in mComplexDrinkHelper.listAlcoholSources.indices) {
            val source = mComplexDrinkHelper.listAlcoholSources[i]
            sourceAbv[i] = source.abv
            sourceAmount[i] = source.amount
            sourceMeasure.add(source.measurement)
        }

        outState.putDoubleArray("sourceAbv", sourceAbv)
        outState.putDoubleArray("sourceAmount", sourceAmount)
        outState.putStringArrayList("sourceMeasure", sourceMeasure)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_drink_menu, menu)
        val item = menu!!.findItem(R.id.btn_toolbar_favorite)
        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_border_white_24dp)
        // if called from profile fragment, the drink will be favorited and cannot be unfavorited
        if (mFavorited) {
            canUnfavorite = false
            item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_white_24dp)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val resId = item.itemId
        val btnAdd = findViewById<MaterialButton>(R.id.btn_add_drink_add)
        when (resId) {
            R.id.btn_toolbar_favorite -> { favoriteOptionSelected(item, btnAdd) }
            R.id.btn_clear_favorites_list -> { clearFavoritesOptionSelected() }
            R.id.btn_clear_recents_list -> { clearRecentsOptionSelected() }
            R.id.btn_toolbar_manage_db -> {
                val intent = Intent(this, ManageDBActivity::class.java)
                startActivity(intent)
            }
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        // make sure source fragment is shown when going back to main activity
        // if you can unfavorite, add drink ws brought up from the home fragment
        // if not it was brought up by the profile fragment
        fragmentId = if (canUnfavorite) 0 else 2
        startActivity(intent)
    }

    private fun favoriteOptionSelected(item: MenuItem, btnAdd: MaterialButton): Boolean {
        if (canUnfavorite) mFavorited = !mFavorited
        if (mFavorited) {
            item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_white_24dp)
            if (canUnfavorite) btnAdd.setText(R.string.add_and_favorite)
            showToast("Drink Will Be Favorited After Adding", true)
            setButtonColor(btnAdd, R.color.colorLightRed)
        } else {
            item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_border_white_24dp)
            btnAdd.setText(R.string.add)
            showToast("Drink Will Not Be Favorited", true)
            setButtonColor(btnAdd, R.color.colorGreen)
        }
        return true
    }

    private fun clearFavoritesOptionSelected(): Boolean {
        if (mFavoritesList.isEmpty()) return false
        val posAction = {
            mDatabaseHelper.deleteRowsInTable("favorites", null)
            mFavoritesList.clear()
            showOrHideEmptyTextViews()
            mFavoritesListAdapter.notifyDataSetChanged()
            mDatabaseHelper.deleteRowsInTable("favorites", null)
        }
        val lightSimpleDialog = LightSimpleDialog(this)
        lightSimpleDialog.setActions(posAction, {})
        lightSimpleDialog.show("Are you sure you want to clear all favorites?")
        return true
    }

    private fun clearRecentsOptionSelected(): Boolean {
        if (mRecentsList.isEmpty()) return false
        val posAction = {
            mDatabaseHelper.deleteRowsInTable("drinks", "recent = 1")
            mRecentsList.clear()
            // todo write UPDATE drinks WHERE recent = 1 SET recent = 0
            showOrHideEmptyTextViews()
            mRecentsListAdapter.notifyDataSetChanged()
        }
        val lightSimpleDialog = LightSimpleDialog(this)
        lightSimpleDialog.setActions(posAction, {})
        lightSimpleDialog.show("Are you sure you want to clear all recent drinks?")
        return true
    }

    private fun toolbarSetup() {
        supportActionBar?.title = "Add Drink"
        // adds back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back_white_24dp)
    }

    private fun setupRecentsAndFavoritesRecycler() {
        // favorites recycler view setup
        val favoriteListView: RecyclerView = findViewById(R.id.recycler_add_drink_favorites_list)
        val linearLayoutManagerFavorites = LinearLayoutManager(this)
        linearLayoutManagerFavorites.orientation = LinearLayoutManager.HORIZONTAL
        favoriteListView.layoutManager = linearLayoutManagerFavorites

        // recents recycler view setup
        val recentsListView: RecyclerView = findViewById(R.id.recycler_add_drink_recents_list)
        val linearLayoutManagerRecents = LinearLayoutManager(this)
        linearLayoutManagerRecents.orientation = LinearLayoutManager.HORIZONTAL
        recentsListView.layoutManager = linearLayoutManagerRecents

        // adapter setup
        mFavoritesListAdapter = AddDrinkActivityFavoritesListAdapter(this)
        favoriteListView.adapter = mFavoritesListAdapter

        // adapter setup
        mRecentsListAdapter = AddDrinkActivityRecentsListAdapter(this)
        recentsListView.adapter = mRecentsListAdapter
    }

    private fun drinkNameEditTextSetup() {
        autoCompleteView = findViewById(R.id.auto_drink_suggestion)

        var drinks = ArrayList<Drink>()

        var adapter = DrinkSuggestionArrayAdapter(this, R.layout.activity_add_drink_suggestion_list, drinks)
        autoCompleteView.setAdapter(adapter)

        // add the listener so it will tries to suggest while the user types
        autoCompleteView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // get new list an apply it
                val temp = mDatabaseHelper.getSuggestedDrinks(s.toString())
                drinks = if (temp.isNotEmpty() || count < 50) temp else drinks
                adapter = DrinkSuggestionArrayAdapter(this@AddDrinkActivity, R.layout.activity_add_drink_suggestion_list, drinks)
                autoCompleteView.setAdapter(adapter)
                adapter.notifyDataSetChanged()
            }
        })

        autoCompleteView.setOnItemClickListener { _, _, position, _ ->
            hideKeyboard(this@AddDrinkActivity)
            val drink = adapter.data[position]
            fillViews(drink.name, drink.abv, drink.amount, drink.measurement)
            autoCompleteView.dismissDropDown()
        }
    }

    private fun hideKeyboard (activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showOrHideEmptyTextViews() {
        val emptyFavorite = findViewById<TextView>(R.id.text_favorites_empty_list)
        val emptyRecent = findViewById<TextView>(R.id.text_recents_empty_list)

        if (mFavoritesList.isEmpty()) {
            emptyFavorite.visibility = View.VISIBLE
        } else {
            emptyFavorite.visibility = View.INVISIBLE
        }

        if (mRecentsList.isEmpty()) {
            emptyRecent.visibility = View.VISIBLE
        } else {
            emptyRecent.visibility = View.INVISIBLE
        }
    }

    private fun resetTextViewStyle(view: TextView, id: Int) {
        view.text = resources.getText(id)
        view.setTypeface(null, Typeface.NORMAL)
        view.setTextColor(ContextCompat.getColor(this, R.color.colorText))
    }

    fun fillViews(name: String, abv: Double, amount: Double, measurement: String) {
        val mEditName = findViewById<EditText>(R.id.auto_drink_suggestion)
        val mEditAbv = findViewById<EditText>(R.id.edit_add_drink_abv)
        val mEditAmount = findViewById<EditText>(R.id.edit_add_drink_amount)
        val dropdown = findViewById<Spinner>(R.id.spinner_add_drink_amount)

        mEditName.setText(name)
        mEditAbv.setText(abv.toString())
        mEditAmount.setText(amount.toString())
        val country = Locale.getDefault().country
        val items = Constants.VOLUME_MEASUREMENTS_METRIC_FIRST
        if (country == "US" || country == "LR" || country == "MM") {
            items[0] = "oz"
            items[1] = "ml"
        }
        dropdown.setSelection(items.indexOf(measurement))
    }

    private fun addDrink() {
        if (isInputErrors() && !complexMode) return
        if (complexMode && mComplexDrinkHelper.listIsEmpty() && isInputErrors()) return
        if (complexMode) mComplexDrinkHelper.addToAlcoholSourceList()

        val name = mEditName.text.toString()
        val abv = if (!complexMode) mEditAbv.text.toString().toDouble() else mComplexDrinkHelper.weightedAverageAbv()
        val amount = if (!complexMode) mEditAmount.text.toString().toDouble() else mComplexDrinkHelper.sumAmount()
        val measurement = if (!complexMode) mSpinnerAmount.selectedItem.toString() else "oz"
        mDatabaseHelper.buildDrinkAndAddToList(name, abv, amount, measurement, mFavorited, canUnfavorite)

        mEditName.text.clear()
        mEditAbv.text.clear()
        mEditAmount.text.clear()
        complexMode = false
        findViewById<CheckBox>(R.id.chkBox_complexDrink).isChecked = false
        val intent = Intent(this, MainActivity::class.java)
        fragmentId = if (canUnfavorite) 0 else 2
        intent.putExtra("drinkAdded", true)
        startActivity(intent)
    }

    fun addDrinkToCurrentSessionAndRecentsTables(drink: Drink) {
        mDatabaseHelper.insertRowInCurrentSessionTable(drink.id, mDatabaseHelper.getNumberOfRows("current_session_drinks").toInt())

        // UPDATE drinks WHERE name = drink.name SET recent = 0
        val args = ContentValues()
        args.put("recent", 0)
        mDatabaseHelper.db.update("drinks", args, "name=?", arrayOf(drink.name))

        // UPDATE drinks WHERE id = drink.id SET recent = 1
        args.clear()
        args.put("recent", 1)
        mDatabaseHelper.db.update("drinks", args, "id=?", arrayOf(drink.id.toString()))

        if (drink.favorited) {
            addToFavoritesTable(drink)
        }
    }

    fun addToFavoritesTable(drink: Drink) {
        mDatabaseHelper.updateDrinkFavoriteStatus(drink)
    }

    fun isInputErrors(): Boolean {
        val textName = findViewById<TextView>(R.id.text_add_drink_name)
        val textABV = findViewById<TextView>(R.id.text_add_drink_abv)
        val textAmount = findViewById<TextView>(R.id.text_add_drink_amount)
        val measurement = findViewById<Spinner>(R.id.spinner_add_drink_amount).selectedItem.toString()

        resetTextViewStyle(textName, R.string.name)
        resetTextViewStyle(textABV, R.string.abv)
        resetTextViewStyle(textAmount, R.string.amount)

        val abv = mConverter.stringToDouble(mEditAbv.text.toString())
        val amount = mConverter.stringToDouble(mEditAmount.text.toString())

        // if valid number after rounding
        if (!abv.isNaN()) mEditAbv.setText(abv.toString())
        if (!amount.isNaN()) mEditAmount.setText(amount.toString())

        var inputError = false
        var message = " "

        // build message to say what is invalid
        val foz = mConverter.drinkVolumeToFluidOz(amount, measurement)
        // 560 fluid oz = 30 pack of beer + 17 beers, more than too much liquid to put in one drink
        if (amount.isNaN() ||foz > 560) {
            message = ", amount$message"
            setTextViewToRedAndBold(textAmount)
            inputError = true
        }

        // abv cant physically be over 100%
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
        if (inputError && (!complexMode || mComplexDrinkHelper.listIsEmpty())) {
            showToast("Please enter a valid ${message.substring(2, message.length)}", false)
        }
        return inputError
    }

    private fun setTextViewToRedAndBold(text: TextView) {
        text.setTypeface(null, Typeface.BOLD)
        text.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
    }

}
