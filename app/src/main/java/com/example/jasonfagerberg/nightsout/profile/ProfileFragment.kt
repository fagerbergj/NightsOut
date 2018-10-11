package com.example.jasonfagerberg.nightsout.profile

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.example.jasonfagerberg.nightsout.R
import java.util.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView

private const val TAG = "ProfileFragment"

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

    private var sex:Boolean? = null
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
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_profile)
        toolbar.inflateMenu(R.menu.profile_menu)

        // recycler v setup
        setupFavoritesRecyclerView(view)

        // empty text v setup
        showOrHideEmptyTextViews(view)

        // adapter setup
        mFavoritesListAdapter = ProfileFragmentFavoritesListAdapter(context!!, mMainActivity.mFavoritesList)
        mFavoritesListView.adapter = mFavoritesListAdapter

        // save button setup
        btnSave = view.findViewById( R.id.btn_profile_save)
        btnSave.setOnClickListener{ _ -> saveProfile(view) }

        // edit Text setup
        mWeightEditText = view.findViewById(R.id.edit_profile_weight)
        mWeightEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() == "") return
                weight = if (p0.toString()[p0!!.length-1] == '.')  ("$p0" + "0").toDouble()
                else p0.toString().toDouble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        // add favorite button setup
        val btnAddFavorite = view.findViewById<MaterialButton>(R.id.btn_profile_add_favorite)
        btnAddFavorite.setOnClickListener{ _ ->
            mMainActivity.addDrinkFragment.mFavorited = true
            mMainActivity.setFragment(mMainActivity.addDrinkFragment)
        }

        return view
    }

    override fun onResume() {
        if(profileInit) {
            sex = mMainActivity.sex!!
            weight = mMainActivity.weight
            weightMeasurement = mMainActivity.weightMeasurement
            mWeightEditText.setText(weight.toString())
            mMainActivity.showBottomNavBar(R.id.bottom_nav_profile)
        } else {
            mMainActivity.hideBottomNavBar()
        }

        Log.v(TAG, "sex = $sex weight = $weight measurement = $weightMeasurement")

        // sex button setup
        setupSexButtons(view!!)

        // spinner setup
        setupSpinner(view!!)

        super.onResume()
    }

    fun hasUnsavedData():Boolean{
        if (!profileInit) return false
        return sex != mMainActivity.sex || weight != mMainActivity.weight ||
                weightMeasurement != mMainActivity.weightMeasurement
    }

    private fun setupFavoritesRecyclerView(view: View){
        mFavoritesListView = view.findViewById(R.id.recycler_profile_favorites_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mFavoritesListView.layoutManager = linearLayoutManager
    }

    private fun pressMaleButton(){
        sex = true
        btnMale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightRed), PorterDuff.Mode.MULTIPLY)
        btnFemale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
    }

    private fun pressFemaleButton(){
        sex = false
        btnFemale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightRed), PorterDuff.Mode.MULTIPLY)
        btnMale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
    }

    private fun saveProfile(view: View){
        val sexText = view.findViewById<TextView>(R.id.text_profile_sex)
        val weightText = view.findViewById<TextView>(R.id.text_profile_weight)
        resetTextView(sexText, R.string.text_sex)
        resetTextView(weightText, R.string.text_weight)
        if(!mWeightEditText.text.isEmpty() && "${mWeightEditText.text}"["${mWeightEditText.text}".length-1] == '.'){
            val w = "${mWeightEditText.text}0"
            mWeightEditText.setText(w)
        }

        if(sex == null && !profileInit){
            showToast("Please Select A Sex")
            showErrorText(sexText, R.string.text_sex_error)
            return
        }else if (mWeightEditText.text.isEmpty() || mWeightEditText.text.toString().toDouble()  < 20 ){
            showErrorText(weightText, R.string.text_weight_error)
            showToast("Please Enter a Valid Weight")
            return
        }

        val weight = mWeightEditText.text.toString().toDouble()
        val weightMeasurement = mSpinner.selectedItem.toString()

        mMainActivity.sex = sex
        mMainActivity.weight = weight
        mMainActivity.weightMeasurement = weightMeasurement

        if (profileInit){
            showToast("Profile Saved!")
            return
        }else{
            mMainActivity.profileInt = true
            mMainActivity.setFragment(mMainActivity.homeFragment)
        }
    }

    private fun resetTextView(view: TextView, id: Int){
        view.text = resources.getText(id)
        view.setTypeface(null, Typeface.NORMAL)
        view.setTextColor(ContextCompat.getColor(context!!, R.color.colorText))
        view.setText(id)
    }

    private fun showErrorText(textView: TextView, errorMessageId: Int){
        textView.text = resources.getText(errorMessageId)
        textView.setTypeface(null, Typeface.BOLD)
        textView.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
    }

    private fun showToast(message: String){
        val toast = Toast.makeText(context!!, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 450)
        toast.show()
    }

    private fun setupSpinner(view: View){
        mSpinner = view.findViewById(R.id.spinner_profile)
        val items = arrayOf("lbs", "kg")
        Log.v(TAG, "items[pos]= ${items[items.indexOf(weightMeasurement)]} pos = ${items.indexOf(weightMeasurement)} weightMeasure = $weightMeasurement")
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

    private fun setupSexButtons(view: View){
        btnMale = view.findViewById(R.id.btn_profile_male)
        btnFemale = view.findViewById(R.id.btn_profile_female)

        if (sex != null && sex!!) {
            pressMaleButton()
        }else if(sex != null && !sex!!){
            pressFemaleButton()
        }

        btnMale.setOnClickListener{ _ ->
            pressMaleButton()
        }

        btnFemale.setOnClickListener{ _ ->
            pressFemaleButton()
        }
    }

    private fun showOrHideEmptyTextViews(view: View){
        val emptyFavorite = view.findViewById<TextView>(R.id.text_profile_favorites_empty_list)

        if(mMainActivity.mFavoritesList.isEmpty()){
            emptyFavorite.visibility = View.VISIBLE
        }else{
            emptyFavorite.visibility = View.INVISIBLE
        }
    }
}