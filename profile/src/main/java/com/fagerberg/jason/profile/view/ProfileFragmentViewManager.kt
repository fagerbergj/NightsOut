package com.fagerberg.jason.profile.view

import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.profile.R
import com.fagerberg.jason.profile.presenter.ProfileViewModel
import com.google.android.material.button.MaterialButton

class ProfileFragmentViewManager(fragment: ProfileFragment) {
    private val activity: NightsOutActivity = fragment.activity as NightsOutActivity

    private val editWeight = activity.findViewById<EditText>(R.id.edit_profile_weight)
    private val spinnerWeightMeasure = activity.findViewById<Spinner>(R.id.spinner_profile)

    private val btnMale = activity.findViewById<MaterialButton>(R.id.btn_profile_male)
    private val btnFemale = activity.findViewById<MaterialButton>(R.id.btn_profile_female)
    private val btnSave = activity.findViewById<MaterialButton>(R.id.btn_profile_save)

    private val recyclerFavorites = activity.findViewById<RecyclerView>(R.id.recycler_profile_favorites_list)
    private lateinit var adapterFavorites : ProfileFragmentFavoritesAdapter

    fun render(viewModel: ProfileViewModel) {

    }
}
