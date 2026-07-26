package com.wit.jasonfagerberg.nightsout.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.settings.SettingsRepository
import org.koin.android.ext.android.inject

/** Fragment shell — hosts the Compose ProfileScreen via a ComposeView. */
class ProfileFragment : Fragment() {

    private lateinit var mMainActivity: MainActivity
    private val settingsRepo by inject<SettingsRepository>()

    private val profileViewModel: ProfileViewModel = ViewModelProvider(
        this,
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(settingsRepo) as T
        }
    )[ProfileViewModel::class.java]

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = requireActivity() as MainActivity
        mMainActivity.profileFragment = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).also { composeView ->
        val acts = ProfileActions(
            context = requireContext(),
            favoritesList = mMainActivity.mFavoritesList,
            drinksList = mMainActivity.mDrinksList,
            onAddFavorite = {
                val intent = Intent(requireContext(), AddDrinkActivity::class.java)
                intent.putExtra("CAN_UNFAVORITE", false)
                intent.putExtra("FAVORITED", true)
                mMainActivity.pushToBackStack(4)
                startActivity(intent)
            },
            clearFavorites = {}
        )
        composeView.setContent {
            CompositionLocalProvider(
                LocalProfileActions provides acts,
                content = { ProfileScreen(viewModel = profileViewModel) })
        }

    }

    /** Mirror of the former Fragment method for MainActivity navigation guard. */
    fun hasUnsavedData(): Boolean =
        mMainActivity.profileInit &&
        profileViewModel.uiState.value.sex != null &&
        profileViewModel.hasUnsavedData(
            currentSex = mMainActivity.sex,
            currentWeight = mMainActivity.weight,
            currentMeas = mMainActivity.weightMeasurement
        )

    /** Called from AddDrinkActivity to refresh UI after a favorite is added. */
    fun showOrHideEmptyTextViews(view: View) { /* Compose handles internally; no-op */ }
}
