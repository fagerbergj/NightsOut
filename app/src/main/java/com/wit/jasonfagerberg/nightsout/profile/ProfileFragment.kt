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
import androidx.lifecycle.lifecycleScope
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.settings.SettingsRepository
import com.wit.jasonfagerberg.nightsout.settings.SettingsShim
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme
  import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Fragment shell — hosts the Compose ProfileScreen via a ComposeView. */
class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModel()
    private val repository: NightsOutRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private var _settingsShim: SettingsShim? = null
    @Suppress("DEPRECATION") // ponytail: onCreateView theme read still synchronous; #54 revisit
    private val settingsShim: SettingsShim get() = _settingsShim!!

    // ponytail: mutable snapshots refreshed from repository on resume. ProFileScreen reads them.
    private val favoritesList: MutableList<Drink> = java.util.ArrayList()
    private val drinksList: MutableList<Drink> = java.util.ArrayList()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _settingsShim = SettingsShim(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).also { composeView ->
        val acts = ProfileActions(
            context = requireContext(),
            favoritesList = favoritesList,
            drinksList = drinksList,
            onAddFavorite = {
                val intent = Intent(requireContext(), AddDrinkActivity::class.java)
                intent.putExtra("CAN_UNFAVORITE", false)
                intent.putExtra("FAVORITED", true)
                (activity as? MainActivity)?.pushToBackStack(4)
                startActivity(intent)
            },
            onRemoveFavorite = { drink ->
                lifecycleScope.launch { repository.deleteFavoriteByName(drink.name); loadFavorites() }
            },
            clearFavorites = {
                lifecycleScope.launch { repository.deleteAllFavorites(); loadFavorites() }
            }
        )
        val themeMode = runCatching { settingsShim.getString(Constants.PREFERENCE.ACTIVE_THEME_MODE, "light") }.getOrDefault("light")
        composeView.setContent {
            NightsOutTheme(darkMode = themeMode == "dark") {
                CompositionLocalProvider(LocalProfileActions provides acts, content = { ProfileScreen(viewModel = profileViewModel) })
            }
        }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            try { favoritesList.clear(); favoritesList.addAll(repository.pullFavoriteDrinks()) } catch (_: Exception) {}
        }
    }

    /** Mirror of the former Fragment method for MainActivity navigation guard. */
    fun hasUnsavedData(): Boolean = run {
        val profileInit = runCatching { runBlocking { settingsRepository.profileInit.first() } }.getOrDefault(false)
        val sexSetting = runCatching { runBlocking { settingsRepository.profileSex.first() } }.getOrDefault(true)
        val weightSetting = runCatching { runBlocking { settingsRepository.profileWeight.first() } }.getOrDefault(0f).toDouble()
        val measSetting = runCatching { runBlocking { settingsRepository.profileWeightMeasurement.first() } }.getOrElse { "lbs" }.takeIf { it.isNotEmpty() } ?: "lbs"
        profileInit &&
        profileViewModel.uiState.value.sex != null &&
        profileViewModel.hasUnsavedData(
            currentSex = sexSetting,
            currentWeight = weightSetting,
            currentMeas = measSetting
        )
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    /** Called from AddDrinkActivity to refresh UI after a favorite is added. */
    fun showOrHideEmptyTextViews(view: View) {}
}
