package com.fagerberg.jason.profile.view

import android.app.Activity
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.fagerberg.jason.profile.R
import com.fagerberg.jason.profile.presenter.ProfileViewModel
import com.google.android.material.button.MaterialButton

class ProfileFragmentViewManager(activity: Activity) {

    val editWeight = activity.findViewById<EditText>(R.id.edit_profile_weight)
    val spinnerWeightMeasure = activity.findViewById<Spinner>(R.id.spinner_profile)

    val btnMale = activity.findViewById<MaterialButton>(R.id.btn_profile_male)
    val btnFemale = activity.findViewById<MaterialButton>(R.id.btn_profile_female)
    val btnSave = activity.findViewById<MaterialButton>(R.id.btn_profile_save)

    val recyclerFavorites = activity.findViewById<RecyclerView>(R.id.recycler_profile_favorites_list)
    private lateinit var adapterFavorites : ProfileFragmentFavoritesAdapter

    fun render(viewModel: ProfileViewModel) {

    }
}
