package com.wit.jasonfagerberg.nightsout.home

import android.app.DatePickerDialog
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
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.dialogs.BacInfoDialog
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.home.ui.HomeScreen
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.manageDB.ManageDBActivity
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme
import com.wit.jasonfagerberg.nightsout.profile.showToast
import com.wit.jasonfagerberg.nightsout.settings.SettingsShim
import com.wit.jasonfagerberg.nightsout.utils.Converter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.wit.jasonfagerberg.nightsout.settings.SettingsRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {
    private lateinit var mLayout: View
    private val repository: NightsOutRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private var _settingsShim: SettingsShim? = null
    @Suppress("DEPRECATION") // ponytail: onCreateView theme read still synchronous; #54 revisit
    private val settingsShim: SettingsShim get() = _settingsShim!!

    val mConverter = Converter()

    private val homeViewModel: HomeViewModel by viewModel()

    val bac: Double get() = homeViewModel.uiState.value.bacValue
    val drinkingDuration: Double get() = homeViewModel.uiState.value.drinkingDuration
    val standardDrinksConsumed: Double get() = homeViewModel.uiState.value.standardDrinksConsumed

    private var bacDialog: BacInfoDialog? = null

    @Suppress("DEPRECATION") // shim phase; #54 replaces with SettingsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _settingsShim = try { SettingsShim(requireContext()) } catch (_: Exception) { SettingsShim(requireActivity().applicationContext) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bacDialog = BacInfoDialog(requireContext())
        val themeMode = runCatching { settingsShim.getString(Constants.PREFERENCE.ACTIVE_THEME_MODE, "light") }.getOrDefault("light")

        val composeView = ComposeView(requireContext()).apply {
            setContent {
                NightsOutTheme(darkMode = themeMode == "dark") {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onAddDrinkClicked = { homeViewModel.onAddDrinkClicked(requireContext()) },
                        onDrinksLoadFailed = { context?.showToast("Couldn't load your drinks - try reopening the app", true) }
                    )
                }
            }
        }

        mLayout = composeView

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { refreshDrinkList() }
        }

        return composeView
    }

    private suspend fun syncViewModelFromSettings(): kotlin.Unit = run {
        val vm = homeViewModel
        try {
            vm.updateProfile(
                sex = settingsRepository.profileSex.first(),
                weight = settingsRepository.profileWeight.first().toDouble(),
                weightMeasurement = if (settingsRepository.profileWeightMeasurement.first().isEmpty()) "lbs" else settingsRepository.profileWeightMeasurement.first()
            )
            val currentTimeInMinutes = Constants.getCurrentTimeInMinuets()
            vm.updateTimeSettings(
                startTimeMin = settingsRepository.startTimeMin.first() ?: currentTimeInMinutes,
                endTimeMin = settingsRepository.endTimeMin.first() ?: currentTimeInMinutes,
                use24HourTime = settingsRepository.use24HourTime.first()
            )
        } catch (_: Exception) {}
    }

    private fun refreshDrinkList() {
        lifecycleScope.launch {
            syncViewModelFromSettings()
            try {
                val drinks = repository.pullCurrentSessionDrinks()
               homeViewModel.addCurrentSessionDrinks(drinks)
            } catch (_: Exception) {}
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDrinkList()
    }

    override fun onStart() {
        super.onStart()
        bacDialog?.let { dialog ->
            if (dialog.isShowing()) dialog.dismiss()
            view?.findViewById<TextView>(R.id.text_home_bac_value)?.setOnClickListener { showBacInfo(dialog) }
            view?.findViewById<TextView>(R.id.text_home_bac_result)?.setOnClickListener { showBacInfo(dialog) }
            dialog.showBacInfoDialog()
        } ?: run {
            bacDialog = BacInfoDialog(context!!)
            val d = bacDialog!!
            view?.findViewById<TextView>(R.id.text_home_bac_value)?.setOnClickListener { showBacInfo(d) }
            view?.findViewById<TextView>(R.id.text_home_bac_result)?.setOnClickListener { showBacInfo(d) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bacDialog?.dismiss()
        bacDialog = null
    }

    private fun showBacInfo(dialog: BacInfoDialog) {
        dialog.setParams(bac, drinkingDuration, standardDrinksConsumed, mConverter, requireContext())
        dialog.showBacInfoDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        (activity as? MainActivity)?.supportActionBar?.title = "Home"
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_toolbar_home_done -> showDatePicker()
            R.id.btn_clear_drink_list -> clearSessionConfirmation()
            R.id.btn_disclaimer -> homeViewModel.showDisclaimer(requireContext())
            R.id.btn_toolbar_manage_db -> startActivity(Intent(activity, ManageDBActivity::class.java))
        }
        return (activity as? MainActivity)?.onOptionsItemSelected(item) ?: false
    }

    fun showOrHideEmptyListText(view: View) {}
    fun refreshBacFromTimeChange() { try { runBlocking { syncViewModelFromSettings() } } catch (_: Exception) {} }

    private fun clearSessionConfirmation() {
        if (homeViewModel.uiState.value.drinks.isEmpty()) return
        val light = LightSimpleDialog(context!!)
        light.setActions({ clearSession() }, { })
        light.show("Are you sure you want to clear all drinks?")
    }

    @Suppress("DEPRECATION") // ponytail: theme read in onCreateView still synchronous; #54 revisit
    private fun clearSession() = run {
        homeViewModel.clearSession()
        try {
            val currentTimeInMinutes = Constants.getCurrentTimeInMinuets()
            runBlocking {
                settingsRepository.setStartTimeMin(currentTimeInMinutes)
                settingsRepository.setEndTimeMin(currentTimeInMinutes)
            }
        } catch (_: Exception) {}
        try { runBlocking { syncViewModelFromSettings() } } catch (_: Exception) {}
        sendBacNotificationAction(Constants.ACTION.STOP_SERVICE)
    }

    private fun sendBacNotificationAction(action: String) {
        try {
            val showNotif = runCatching { runBlocking { settingsRepository.showBacNotification.first() } }.getOrDefault(true)
            if (showNotif) {
                val intent = Intent(requireContext(), com.wit.jasonfagerberg.nightsout.notification.BacNotificationService::class.java).apply { this.action = action }
                requireContext().startService(intent)
            }
        } catch (_: Exception) {}
    }

    private fun showDatePicker() {
        val cal = java.util.Calendar.getInstance()
        DatePickerDialog(requireContext(), null, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
            .also { dp ->
                dp.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK") { _, _ ->
                    val logDate = Integer.parseInt(mConverter.yearMonthDayTo8DigitString(dp.datePicker.year, dp.datePicker.month, dp.datePicker.dayOfMonth))
                    handleLogDateSelection(logDate)
                    dp.dismiss()
                }
                dp.setTitle("Log Day")
                dp.show()
            }
    }

    private fun handleLogDateSelection(logDate: Int) {
        when (val action = homeViewModel.checkOrCreateLog(logDate)) {
            is HomeViewModel.LogAction.CreateNewLog -> confirmCreateLogDialog(logDate)
            is HomeViewModel.LogAction.UpdateExistingLog -> showUpdateLogConfirmationDialog(logDate, action.existingBac, action.existingDuration)
            null -> {}
        }
    }

    private fun confirmCreateLogDialog(logDate: Int) {
        val light = LightSimpleDialog(context!!)
        val posAction: () -> Unit = { lifecycleScope.launch { doLogSession(logDate) } }
        light.setActions(posAction, { })
        light.show("Do you want to start a new drink list?")
    }

    private suspend fun doLogSession(logDate: Int) {
        val success = homeViewModel.logSessionWithDate(logDate)
        if (success) context?.showToast("Logged", false) else context?.showToast("Nothing to log", true)
    }

    private fun showUpdateLogConfirmationDialog(logDate: Int, existingBac: Double, existingDuration: Double) {
        val header = LogHeader(logDate, existingBac, existingDuration)
        val simpleDialog = SimpleDialog(requireContext(), requireActivity().layoutInflater)
        simpleDialog.setTitle(resources.getString(R.string.update_log))
        simpleDialog.setBody("There is already a log on ${header.monthName} ${header.day}, ${header.year}. Would you like to update the old log?")
        simpleDialog.setNegativeButtonText(resources.getString(R.string.cancel))
        simpleDialog.setNegativeFunction {
            showDatePicker()
            // SimpleDialog shows itself in init — dismiss via reflection or remove this line
        }
        val thisDate = logDate
        simpleDialog.setPositiveButtonText(resources.getString(R.string.update))
        simpleDialog.setPositiveFunction {
            lifecycleScope.launch { homeViewModel.logSessionWithDate(thisDate) }
            // Auto-show, auto-dismiss pattern handled by SimpleDialog
        }
    }

    private fun showDisclaimerDialog() {
        val dialog = SimpleDialog(context!!, layoutInflater)
        dialog.setTitle(getString(R.string.disclaimer))
        dialog.setBody(getString(R.string.disclaimer_body))
        dialog.setNeutralFunction { dialog.dismiss() }
    }
}
