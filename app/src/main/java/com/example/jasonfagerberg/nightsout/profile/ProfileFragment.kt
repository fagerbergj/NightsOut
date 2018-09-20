package com.example.jasonfagerberg.nightsout.profile

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.example.jasonfagerberg.nightsout.R

private const val TAG = "ProfileFragment"

class ProfileFragment : Fragment() {

    private lateinit var mFavoritesListAdapter: ProfileFragmentFavoritesListAdapter
    private lateinit var mMainActivity: MainActivity
    private lateinit var mFavoritesListView: RecyclerView

    // shared pref data
    private var profileInit = false
    private var sex: Boolean = true
    private lateinit var mWeightEditText: EditText
    private lateinit var mSpinner: Spinner

    // a sex button is selected
    private var sexButtonPressed = false

    private lateinit var btnMale: MaterialButton
    private lateinit var btnFemale: MaterialButton
    private lateinit var btnSave: MaterialButton


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        mMainActivity = context as MainActivity
        profileInit = mMainActivity.profileInt
        sex = mMainActivity.sex

        //toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_profile)
        toolbar.inflateMenu(R.menu.empty_menu)

        // spinner setup
        setupSpinner(view)

        // recycler view setup
        mFavoritesListView = view.findViewById(R.id.recycler_profile_favorites_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mFavoritesListView.layoutManager = linearLayoutManager

        // empty text view setup
        showOrHideEmptyTextViews(view)

        // adapter setup
        mFavoritesListAdapter = ProfileFragmentFavoritesListAdapter(context!!, mMainActivity.mFavoritesList)
        mFavoritesListView.adapter = mFavoritesListAdapter

        // save button setup
        btnSave = view.findViewById(R.id.btn_profile_save)
        btnSave.setOnClickListener{ _ ->
            saveProfile(view)
        }

        // sex button setup
        setupSexButtons(view)

        // edit Text setup
        mWeightEditText = view.findViewById(R.id.edit_profile_weight)
        if(profileInit) mWeightEditText.setText(mMainActivity.weight.toString())

        if(profileInit) {
            mMainActivity.showBottomNavBar(R.id.bottom_nav_profile)
        } else {
            mMainActivity.hideBottomNavBar()
        }

        // add favorite button setup
        val btnAddFavorite = view.findViewById<MaterialButton>(R.id.btn_profile_add_favorite)
        btnAddFavorite.setOnClickListener{ _ ->
            mMainActivity.addDrinkFragment.mFavorited = true
            mMainActivity.setFragment(mMainActivity.addDrinkFragment)
        }

        return view
    }

    override fun onPause() {
        Log.v(TAG, mFavoritesListView.height.toString())
        super.onPause()
    }

    private fun pressMaleButton(){
        sexButtonPressed = true
        btnMale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightRed), PorterDuff.Mode.MULTIPLY)
        btnFemale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
    }

    private fun pressFemaleButton(){
        sexButtonPressed = true
        btnFemale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightRed), PorterDuff.Mode.MULTIPLY)
        btnMale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
    }

    private fun saveProfile(view: View){
        val sexText = view.findViewById<TextView>(R.id.text_profile_sex)
        if(!mWeightEditText.text.isEmpty() && "${mWeightEditText.text}"["${mWeightEditText.text}".length-1] == '.'){
            val w = "${mWeightEditText.text}0"
            mWeightEditText.setText(w)
        }

        if(!sexButtonPressed && !profileInit){
            val toast = Toast.makeText(context!!, "Please Select a Sex", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 450)
            toast.show()
            sexText.text = resources.getText(R.string.text_sex_error)
            sexText.setTypeface(null, Typeface.BOLD)
            sexText.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
            return
        }else if (mWeightEditText.text.isEmpty() || mWeightEditText.text.toString().toDouble()  < 60 ){
            val weightText = view.findViewById<TextView>(R.id.text_profile_weight)
            // make weight text stand out
            weightText.text = resources.getText(R.string.text_weight_error)
            weightText.setTypeface(null, Typeface.BOLD)
            val oldColors = weightText.textColors //save original colors
            weightText.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))

            // make sex text normal
            sexText.text = resources.getText(R.string.text_sex)
            sexText.setTypeface(null, Typeface.NORMAL)
            sexText.setTextColor(oldColors)

            val toast = Toast.makeText(context!!, "Please Enter a Valid Weight", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 450)
            toast.show()
            return
        }

        val weight = mWeightEditText.text.toString().toDouble()
        val weightMeasurement = mSpinner.selectedItem.toString()

        mMainActivity.sex = sex
        mMainActivity.weight = weight
        mMainActivity.weightMeasurement = weightMeasurement
        Log.v(TAG, "Passed vars: sex=$sex weight=$weight weight " +
                "measurement=$weightMeasurement profileInit=${mMainActivity.profileInt}")

        if (profileInit){
            val toast = Toast.makeText(context!!, "Profile Saved!", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 450)
            toast.show()
            return
        }else{
            mMainActivity.profileInt = true
            mMainActivity.setFragment(mMainActivity.homeFragment)
        }
    }

    private fun setupSpinner(view: View){
        mSpinner = view.findViewById(R.id.spinner_profile)
        val items = arrayOf("lbs", "kg")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, items)
        mSpinner.adapter = adapter
    }

    private fun setupSexButtons(view: View){
        btnMale = view.findViewById(R.id.btn_profile_male)
        btnFemale = view.findViewById(R.id.btn_profile_female)

        if (profileInit && sex) {
            pressMaleButton()
        }else if(profileInit && !sex){
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
