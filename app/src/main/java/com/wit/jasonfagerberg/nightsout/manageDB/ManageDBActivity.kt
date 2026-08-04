package com.wit.jasonfagerberg.nightsout.manageDB

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.main.NightsOutActivity
import com.wit.jasonfagerberg.nightsout.manageDB.ui.ManageDBScreen
import com.wit.jasonfagerberg.nightsout.manageDB.ui.ManageDBViewModel
import com.wit.jasonfagerberg.nightsout.models.Drink
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ManageDBActivity : NightsOutActivity() {

    private val repository: NightsOutRepository by inject()
    private var _viewModel: ManageDBViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ManageDBViewModelFactory(repository)
        setContentView(ComposeView(this).apply {
            setContent {
                ManageDBScreen(ManageDBViewModel(repository), ::onBack, ::onDeleteConfirmed)
            }
        })
        _viewModel = ManageDBViewModel(repository)
    }

    private fun onBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    /** Handles async delete: computes reference-loss string then shows alert dialog. */
    private fun onDeleteConfirmed(drink: Drink) {
        lifecycleScope.launch {
            val vm = _viewModel!!
            val loss = vm.getLostReferenceString(drink)
            AlertDialog.Builder(this@ManageDBActivity).run {
                setTitle(getString(R.string.delete_drink))
                setMessage(
                    getString(R.string.delete_drink) + " \"" + drink.name + "\" from database, " +
                            "this will remove all references to the drink.\n\nReferences Lost:\n" + loss
                )
                setPositiveButton(getString(R.string.yes)) { _, _ -> vm.deleteDrink(drink) }
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

/** Factory for ManageDBViewModel since it has a non-default constructor. */
class ManageDBViewModelFactory(private val repository: NightsOutRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageDBViewModel::class.java)) {
            return ManageDBViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
