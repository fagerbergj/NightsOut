package com.example.jasonfagerberg.nightsout.profile

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
//import android.util.Log
import android.view.*
import android.widget.*
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.example.jasonfagerberg.nightsout.R
import java.util.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView
import com.example.jasonfagerberg.nightsout.converter.Converter
import com.example.jasonfagerberg.nightsout.dialogs.LightSimpleDialog

//private const val TAG = "ProfileFragment"

class ProfileFragment : Fragment() {
    private lateinit var mFavoritesListAdapter: ProfileFragmentFavoritesListAdapter
    private lateinit var mMainActivity: MainActivity
    private lateinit var mFavoritesListView: RecyclerView

    // shared pref data
    private var profileInit = false

    private lateinit var mWeightEditText: EditText
    private lateinit var mSpinner: Spinner

    private lateinit var btnMale: MaterialButton
    private lateinit var btnFemale: MaterialButton
    private lateinit var btnSave: MaterialButton

    private var sex: Boolean? = null
    private var weight = 0.0

    private val country = Locale.getDefault().country
    private var weightMeasurement = if (country == "US" || country == "LR" || country == "MM") "lbs"
    else "kg"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        mMainActivity = context as MainActivity
        profileInit = mMainActivity.profileInt

        //toolbar setup
        setupToolbar(view!!)

        // recycler v setup
        setupFavoritesRecyclerView(view)

        // empty text v setup
        showOrHideEmptyTextViews(view)

        // adapter setup
        mFavoritesListAdapter = ProfileFragmentFavoritesListAdapter(context!!, mMainActivity.mFavoritesList)
        mFavoritesListView.adapter = mFavoritesListAdapter

        // save button setup
        btnSave = view.findViewById(R.id.btn_profile_save)
        btnSave.setOnClickListener { _ -> saveProfile(view) }

        // edit Text setup
        mWeightEditText = view.findViewById(R.id.edit_profile_weight)
        mWeightEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() == "") return
                weight = if (p0.toString()[p0!!.length - 1] == '.') ("$p0" + "0").toDouble()
                else p0.toString().toDouble()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        // add favorite button setup
        val btnAddFavorite = view.findViewById<MaterialButton>(R.id.btn_profile_add_favorite)
        btnAddFavorite.setOnClickListener { _ ->
            mMainActivity.addDrinkFragment.mFavorited = true
            mMainActivity.setFragment(mMainActivity.addDrinkFragment)
        }

        return view
    }

    override fun onResume() {
        if (profileInit) {
            sex = mMainActivity.sex!!
            weight = mMainActivity.weight
            weightMeasurement = mMainActivity.weightMeasurement
            mWeightEditText.setText(weight.toString())
            mMainActivity.showBottomNavBar(R.id.bottom_nav_profile)
        } else {
            mMainActivity.hideBottomNavBar()
        }

        // sex button setup
        setupSexButtons(view!!)

        // spinner setup
        setupSpinner(view!!)

        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        when (resId) {
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
        }
        return true
    }

    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_profile)
        toolbar.inflateMenu(R.menu.profile_menu)
        mMainActivity.setSupportActionBar(toolbar)
        mMainActivity.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun setupFavoritesRecyclerView(view: View) {
        mFavoritesListView = view.findViewById(R.id.recycler_profile_favorites_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mFavoritesListView.layoutManager = linearLayoutManager
    }

    private fun setupSpinner(view: View) {
        mSpinner = view.findViewById(R.id.spinner_profile)
        val items = arrayOf("lbs", "kg")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, items)
        mSpinner.adapter = adapter
        mSpinner.setSelection(items.indexOf(weightMeasurement))

        mSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                weightMeasurement = selectedItem
            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSexButtons(view: View) {
        btnMale = view.findViewById(R.id.btn_profile_male)
        btnFemale = view.findViewById(R.id.btn_profile_female)

        if (sex != null && sex!!) {
            pressMaleButton()
        } else if (sex != null && !sex!!) {
            pressFemaleButton()
        }

        btnMale.setOnClickListener { _ ->
            pressMaleButton()
        }

        btnFemale.setOnClickListener { _ ->
            pressFemaleButton()
        }
    }

    private fun resetTextView(view: TextView, id: Int) {
        view.text = resources.getText(id)
        view.setTypeface(null, Typeface.NORMAL)
        view.setTextColor(ContextCompat.getColor(context!!, R.color.colorText))
        view.setText(id)
    }

    private fun pressMaleButton() {
        sex = true
        btnMale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightRed), PorterDuff.Mode.MULTIPLY)
        btnFemale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
    }

    private fun pressFemaleButton() {
        sex = false
        btnFemale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightRed), PorterDuff.Mode.MULTIPLY)
        btnMale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
    }

    private fun saveProfile(view: View) {
        val sexText = view.findViewById<TextView>(R.id.text_profile_sex)
        val weightText = view.findViewById<TextView>(R.id.text_profile_weight)
        resetTextView(sexText, R.string.sex)
        resetTextView(weightText, R.string.weight)

        val w = Converter().stringToDouble(mWeightEditText.text.toString())
        if (!w.isNaN()) mWeightEditText.setText(w.toString())

        if (sex == null && !profileInit) {
            mMainActivity.showToast("Please Select A Sex")
            showErrorText(sexText)
            return
        } else if (w.isNaN() || w < 20) {
            showErrorText(weightText)
            mMainActivity.showToast("Please Enter a Valid Weight")
            return
        }

        val weight = mWeightEditText.text.toString().toDouble()
        val weightMeasurement = mSpinner.selectedItem.toString()

        mMainActivity.sex = sex
        mMainActivity.weight = weight
        mMainActivity.weightMeasurement = weightMeasurement

        if (profileInit) {
            mMainActivity.showToast("Profile Saved!")
            return
        } else {
            mMainActivity.profileInt = true
            mMainActivity.setFragment(mMainActivity.homeFragment)
        }
    }

    private fun showErrorText(textView: TextView) {
        textView.setTypeface(null, Typeface.BOLD)
        textView.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
    }

    private fun showOrHideEmptyTextViews(view: View) {
        val emptyFavorite = view.findViewById<TextView>(R.id.text_profile_favorites_empty_list)

        if (mMainActivity.mFavoritesList.isEmpty()) {
            emptyFavorite.visibility = View.VISIBLE
        } else {
            emptyFavorite.visibility = View.INVISIBLE
        }
    }

    fun hasUnsavedData(): Boolean {
        if (!profileInit) return false
        return sex != mMainActivity.sex || weight != mMainActivity.weight ||
                weightMeasurement != mMainActivity.weightMeasurement
    }
}
