package com.wit.jasonfagerberg.nightsout.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.dialogs.BacInfoDialog
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.home.ui.HomeScreen
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.manageDB.ManageDBActivity
import com.wit.jasonfagerberg.nightsout.utils.Converter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var mLayout: View
    private lateinit var mMainActivity: MainActivity
    val mConverter = Converter()

    // ViewModel's uiState is the single source of truth for drinks/BAC/time; these just expose it
    // to callers outside Compose (BacInfoDialog) under the fragment's pre-existing field names.
    val bac: Double get() = homeViewModel?.uiState?.value?.bacValue ?: 0.0
    val drinkingDuration: Double get() = homeViewModel?.uiState?.value?.drinkingDuration ?: 0.0
    val standardDrinksConsumed: Double get() = homeViewModel?.uiState?.value?.standardDrinksConsumed ?: 0.0

    private var homeViewModel: HomeViewModel? = null

    private lateinit var _homeFragmentLogDatePicker: HomeFragmentLogDatePicker
    val homeFragmentLogDatePicker get() = _homeFragmentLogDatePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = requireActivity() as MainActivity
        mMainActivity.homeFragment = this
        setHasOptionsMenu(true)
        @Suppress("UNCHECKED_CAST")
        homeViewModel = org.koin.core.context.GlobalContext.get().get(HomeViewModel::class) as HomeViewModel

        _homeFragmentLogDatePicker = HomeFragmentLogDatePicker(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                HomeScreen(
                    viewModel = homeViewModel!!,
                    onAddDrinkClicked = { homeViewModel!!.onAddDrinkClicked(requireContext()) },
                    onDrinksLoadFailed = {
                        mMainActivity.showToast("Couldn't load your drinks - try reopening the app", true)
                    }
                )
            }
        }

        mLayout = composeView

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                refreshDrinkList()
            }
        }

        return composeView
    }

    // MainActivity/SettingsShim, not the ViewModel, owns profile + time-window prefs (see #54);
    // push them in before recomputing BAC so the ViewModel's calc uses current values.
    private fun syncViewModelFromMainActivity() {
        val vm = homeViewModel ?: return
        vm.updateProfile(
            sex = mMainActivity.sex ?: true,
            weight = mMainActivity.weight,
            weightMeasurement = mMainActivity.weightMeasurement
        )
        vm.updateTimeSettings(
            startTimeMin = mMainActivity.startTimeMin,
            endTimeMin = mMainActivity.endTimeMin,
            use24HourTime = mMainActivity.use24HourTime
        )
    }

    private fun refreshDrinkList() {
        lifecycleScope.launch {
            syncViewModelFromMainActivity()
            val drinks = mMainActivity.repository.pullCurrentSessionDrinks()
            mMainActivity.mDrinksList.clear()
            mMainActivity.mDrinksList.addAll(drinks)
            homeViewModel?.addCurrentSessionDrinks(drinks)
            mMainActivity.sendActionToBacNotificationService(Constants.ACTION.UPDATE_NOTIFICATION)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDrinkList()
    }

    override fun onStart() {
        super.onStart()
        val bacInfoDialog = BacInfoDialog(context!!)
        view?.findViewById<TextView>(R.id.text_home_bac_value)?.setOnClickListener {
            bacInfoDialog.showBacInfoDialog()
        }
        view?.findViewById<TextView>(R.id.text_home_bac_result)?.setOnClickListener {
            bacInfoDialog.showBacInfoDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        mMainActivity.supportActionBar?.title = "Home"
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val resId = item.itemId
        when (resId) {
            R.id.btn_toolbar_home_done -> homeFragmentLogDatePicker.showDatePicker()
            R.id.btn_clear_drink_list -> {
                if (mMainActivity.mDrinksList.isEmpty()) return false
                val lightSimpleDialog = LightSimpleDialog(context!!)
                val posAction = { clearSession() }
                lightSimpleDialog.setActions(posAction, {})
                lightSimpleDialog.show("Are you sure you want to clear all drinks?")
            }
            R.id.btn_disclaimer -> showDisclaimerDialog()
            R.id.btn_toolbar_manage_db -> {
                val intent = Intent(mMainActivity, ManageDBActivity::class.java)
                mMainActivity.startActivity(intent)
            }
        }
        return mMainActivity.onOptionsItemSelected(item)
    }

    fun showOrHideEmptyListText(view: View) {
        // Compose handles empty state internally, this is kept for legacy compatibility
    }

    /** Called by BacNotificationService after it advances the end time in the background. */
    fun refreshBacFromTimeChange() {
        syncViewModelFromMainActivity()
    }

    fun clearSession() {
        mMainActivity.mDrinksList.clear()
        homeViewModel?.clearSession()
        showOrHideEmptyListText(view ?: return)
        mMainActivity.resetTime()
        syncViewModelFromMainActivity()
        mMainActivity.sendActionToBacNotificationService(Constants.ACTION.STOP_SERVICE)
    }

    private fun showDisclaimerDialog() {
        val dialog = SimpleDialog(context!!, layoutInflater)
        dialog.setTitle(getString(R.string.disclaimer))
        dialog.setBody(getString(R.string.disclaimer_body))
        dialog.setNeutralFunction { dialog.dismiss() }
    }
}
