package com.fagerberg.jason.profile.view

import android.graphics.Typeface
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.common.android.NightsOutSharedPreferences
import com.fagerberg.jason.common.constants.WEIGHT_MEASUREMENTS
import com.fagerberg.jason.common.models.Drink
import com.fagerberg.jason.common.models.WeightMeasurement
import com.fagerberg.jason.profile.R
import com.fagerberg.jason.profile.presenter.ProfileIntent
import com.fagerberg.jason.profile.presenter.ProfileViewModel
import com.google.android.material.button.MaterialButton

class ProfileFragmentViewManager(private val fragment: ProfileFragment) {
    private val activity: NightsOutActivity = fragment.activity as NightsOutActivity

    private val textSex = activity.findViewById<TextView>(R.id.text_profile_sex)
    private val textWeight = activity.findViewById<TextView>(R.id.text_profile_weight)

    private val editWeight = activity.findViewById<EditText>(R.id.edit_profile_weight)
    private val spinnerWeightMeasure = activity.findViewById<Spinner>(R.id.spinner_profile).apply {
        adapter = ArrayAdapter(
            fragment.requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            WEIGHT_MEASUREMENTS
        )
        setSelection(0)
    }

    private val btnMale = activity.findViewById<MaterialButton>(R.id.btn_profile_male).apply {
        setOnClickListener { fragment.sendIntent(ProfileIntent.SelectSex(true)) }
    }
    private val btnFemale = activity.findViewById<MaterialButton>(R.id.btn_profile_female).apply {
        setOnClickListener { fragment.sendIntent(ProfileIntent.SelectSex(false)) }
    }
    private val btnSave = activity.findViewById<MaterialButton>(R.id.btn_profile_save).apply {
        setOnClickListener {
            fragment.sendIntent(
                ProfileIntent.Save(
                    activity = activity,
                    sex = btnMale.backgroundTintList == ContextCompat.getColorStateList(fragment.requireContext(), R.color.colorLightRed),
                    weight = editWeight.text.toString().toDouble(),
                    weightMeasurement = WeightMeasurement.fromLowercaseString(spinnerWeightMeasure.selectedItem.toString())
                )
            )
        }
    }

    private val recyclerFavorites =
        activity.findViewById<RecyclerView>(R.id.recycler_profile_favorites_list).apply {
            layoutManager = LinearLayoutManager(fragment.requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
        }
    private lateinit var adapterFavorites: ProfileFragmentFavoritesAdapter

    fun render(viewModel: ProfileViewModel) =
        when(viewModel) {
            // init view models
            is ProfileViewModel.Empty -> {}
            is ProfileViewModel.Init -> renderPreferences(viewModel.sharedPreferences)
            is ProfileViewModel.InitFavorites -> renderFavorites(viewModel.favorites)

            // button presses
            is ProfileViewModel.SelectSex -> renderSelectSex(viewModel.sex)
            is ProfileViewModel.Save -> renderPreferences(viewModel.sharedPreferences) // TODO different render state?
            is ProfileViewModel.InvalidSave -> renderInvalidSave(
                isInvalidSex = viewModel.isInvalidSex,
                isInvalidWeight = viewModel.isInvalidWeight
            )

            // options selected
            ProfileViewModel.ClearFavorites -> renderFavorites(listOf()) // TODO do I have to tell home fragment about this?
            ProfileViewModel.Settings -> { /* TODO tell fragment to go to settings activity */ }

            // recycler item option selected
            is ProfileViewModel.RemoveFavorite -> renderRemoveFavorite(viewModel.drink)
        }

    private fun renderPreferences(sharedPreferences: NightsOutSharedPreferences) {
        fragment.isProfileCreated = sharedPreferences.profileInit
        sharedPreferences.sex?.let { renderSelectSex(sharedPreferences.sex!!) }
        editWeight.setText(sharedPreferences.weight.toString())
        spinnerWeightMeasure.setSelection(WEIGHT_MEASUREMENTS.indexOf(sharedPreferences.weightMeasurement.displayName))
    }

    private fun renderFavorites(favorites: List<Drink>) {
        adapterFavorites = ProfileFragmentFavoritesAdapter(fragment, favorites.toMutableList())
        recyclerFavorites.adapter = adapterFavorites
    }

    private fun renderSelectSex(sex: Boolean) =
        if (sex) {
            activity.setButtonColor(btnMale, R.color.colorLightRed)
            activity.setButtonColor(btnFemale, R.color.colorLightGray)
        } else {
            activity.setButtonColor(btnMale, R.color.colorLightGray)
            activity.setButtonColor(btnFemale, R.color.colorLightRed)
        }

    private fun renderInvalidSave(isInvalidSex: Boolean, isInvalidWeight: Boolean) =
        if (isInvalidSex) {
            activity.showToast("Please Select A Sex", Toast.LENGTH_LONG)
            textSex.setTypeface(null, Typeface.BOLD)
            textSex.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.colorRed))
        } else if (isInvalidWeight) {
            activity.showToast("Please Enter a Valid Weight", Toast.LENGTH_LONG)
            textWeight.setTypeface(null, Typeface.BOLD)
            textWeight.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.colorRed))
        } else {
            throw IllegalArgumentException("At least one input must be invalid to render this state")
        }

    private fun renderRemoveFavorite(removed: Drink) {
        // TODO do I have to tell home fragment about this?
        val index = adapterFavorites.drinksList.indexOf(removed)
        adapterFavorites.drinksList.removeAt(index)
        adapterFavorites.notifyItemRemoved(index)
        adapterFavorites.notifyItemRangeRemoved(index, 1)
    }
}
