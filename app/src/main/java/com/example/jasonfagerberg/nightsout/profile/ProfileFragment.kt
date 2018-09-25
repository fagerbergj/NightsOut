package com.example.jasonfagerberg.nightsout.profile

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.os.Message
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
    /* todo change profile behavior, pull shared prefs onCreate
    push to shared prefs when save button is pressed,
    if add favorites button is pressed, save partial changes in some sort of cache*/

    private lateinit var mFavoritesListAdapter: ProfileFragmentFavoritesListAdapter
    private lateinit var mMainActivity: MainActivity
    private lateinit var mFavoritesListView: RecyclerView

    // shared pref data
    private var profileInit = false
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

        //toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_profile)
        toolbar.inflateMenu(R.menu.profile_menu)

        // spinner setup
        setupSpinner(view)

        // recycler view setup
        setupFavoritesRecyclerView(view)

        // empty text view setup
        showOrHideEmptyTextViews(view)

        // adapter setup
        mFavoritesListAdapter = ProfileFragmentFavoritesListAdapter(context!!, mMainActivity.mFavoritesList)
        mFavoritesListView.adapter = mFavoritesListAdapter

        // save button setup
        btnSave = view.findViewById(R.id.btn_profile_save)
        btnSave.setOnClickListener{ _ -> saveProfile(view) }

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

    private fun setupFavoritesRecyclerView(view: View){
        mFavoritesListView = view.findViewById(R.id.recycler_profile_favorites_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mFavoritesListView.layoutManager = linearLayoutManager
    }

    override fun onPause() {
        Log.v(TAG, mFavoritesListView.height.toString())
        super.onPause()
    }

    private fun pressMaleButton(){
        sexButtonPressed = true
        mMainActivity.sex = true
        btnMale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightRed), PorterDuff.Mode.MULTIPLY)
        btnFemale.background.setColorFilter(ContextCompat.getColor(context!!,
                R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
    }

    private fun pressFemaleButton(){
        sexButtonPressed = true
        mMainActivity.sex = false
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

        if(!sexButtonPressed && !profileInit){
            showToast("Please Select A Sex")
            showErrorText(sexText, R.string.text_sex_error)
            return
        }else if (mWeightEditText.text.isEmpty() || mWeightEditText.text.toString().toDouble()  < 60 ){
            showErrorText(weightText, R.string.text_weight_error)

            showToast("Please Enter a Valid Weight")
            return
        }

        val weight = mWeightEditText.text.toString().toDouble()
        val weightMeasurement = mSpinner.selectedItem.toString()

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
        mSpinner.setSelection(items.indexOf(mMainActivity.weightMeasurement))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, items)
        mSpinner.adapter = adapter
    }

    private fun setupSexButtons(view: View){
        btnMale = view.findViewById(R.id.btn_profile_male)
        btnFemale = view.findViewById(R.id.btn_profile_female)

        if (sexButtonPressed || profileInit && mMainActivity.sex) {
            pressMaleButton()
        }else if(sexButtonPressed || profileInit && !mMainActivity.sex){
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
