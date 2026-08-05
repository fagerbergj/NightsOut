package com.wit.jasonfagerberg.nightsout.manageDB

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.main.NightsOutActivity
import com.wit.jasonfagerberg.nightsout.manageDB.ui.ManageDBScreen
import com.wit.jasonfagerberg.nightsout.manageDB.ui.ManageDBViewModel
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.settings.SettingsShim
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageDBActivity : NightsOutActivity() {

    private val repository: NightsOutRepository by inject()
    private val viewModel: ManageDBViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeMode = runCatching { SettingsShim(this).getString(Constants.PREFERENCE.ACTIVE_THEME_MODE, "light") }.getOrDefault("light")
        setContentView(ComposeView(this).apply {
            setContent {
                NightsOutTheme(darkMode = themeMode == "dark") {
                    ManageDBScreen(viewModel, ::onBack, ::onDeleteConfirmed)
                }
            }
        })
    }

    private fun onBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    /** Handles async delete: computes reference-loss string then shows alert dialog. */
    private fun onDeleteConfirmed(drink: Drink) {
        lifecycleScope.launch {
            val loss = viewModel.getLostReferenceString(drink)
            AlertDialog.Builder(this@ManageDBActivity).run {
                setTitle(getString(R.string.delete_drink))
                setMessage(
                    getString(R.string.delete_drink) + " \"" + drink.name + "\" from database, " +
                            "this will remove all references to the drink.\n\nReferences Lost:\n" + loss
                )
                setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.deleteDrink(drink) }
                setNegativeButton(getString(R.string.no), null)
                show()
            }
        }
    }

    override fun onStart() {
        supportActionBar?.title = "Manage Database"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back_white_24dp)
        super.onStart()
    }
}
