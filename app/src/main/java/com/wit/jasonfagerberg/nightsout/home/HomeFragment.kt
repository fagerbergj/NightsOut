package com.wit.jasonfagerberg.nightsout.home

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.dialogs.BacInfoDialog
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.home.ui.HomeScreen
import com.wit.jasonfagerberg.nightsout.domain.BacCalculator
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.manageDB.ManageDBActivity
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.utils.Converter
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class HomeFragment : Fragment() {
    private lateinit var mLayout: View
    private lateinit var mMainActivity: MainActivity
    val mConverter = Converter()

    var drinkingDuration = 0.0
    var standardDrinksConsumed = 0.0
    var bac: Double = 0.0
        private set

    // State exposed to Compose UI - mirrors ViewModel for composables
    private val _drinks = MutableStateFlow<List<Drink>>(emptyList())
    val drinks: StateFlow<List<Drink>> = _drinks

    private val _bacValue = MutableStateFlow(0.0)
    val bacValue: StateFlow<Double> = _bacValue

    private val _bacState = MutableStateFlow<BacState>(BacState.Sober)
    val bacState: StateFlow<BacState> = _bacState

    private val _timeSettings = MutableStateFlow(TimeSettings(-1, -1, false))
    val timeSettings: StateFlow<TimeSettings> = _timeSettings

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
                    drinks = _drinks.value,
                    bacValue = _bacValue.value,
                    bacState = _bacState.value,
                    timeSettings = _timeSettings.value,
                    onAddDrinkClicked = { homeViewModel!!.onAddDrinkClicked(requireContext()) },
                    onStartTimeChanged = { startMin ->
                        val current = _timeSettings.value
                        homeViewModel!!.updateTimeSettings(
                            startTimeMin = startMin,
                            endTimeMin = if (current.endTimeMin == -1) startMin else current.endTimeMin
                        )
                        _timeSettings.tryEmit(current.copy(startTimeMin = startMin))
                    },
                    onEndTimeChanged = { endMin ->
                        homeViewModel!!.updateTimeSettings(
                            startTimeMin = _timeSettings.value.startTimeMin,
                            endTimeMin = endMin
                        )
                        val current = _timeSettings.value
                        _timeSettings.tryEmit(current.copy(endTimeMin = endMin))
                    },
                    onDeleteDrinkAt = { idx -> homeViewModel!!.deleteDrinkAt(idx) },
                    onFavoriteToggle = { drink -> homeViewModel!!.onFavoriteClicked(drink) }
                )
            }
        }

        mLayout = composeView

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                refreshDrinkList()
                _timeSettings.tryEmit(TimeSettings(
                    mMainActivity.startTimeMin,
                    mMainActivity.endTimeMin,
                    mMainActivity.use24HourTime
                ))
            }
        }

        return composeView
    }

    private fun refreshDrinkList() {
        lifecycleScope.launch {
            val drinks = mMainActivity.repository.pullCurrentSessionDrinks()
            mMainActivity.mDrinksList.clear()
            mMainActivity.mDrinksList.addAll(drinks)
            _drinks.tryEmit(drinks.toList())
            updateBACDisplay(calculateBACInternal(drinks))
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            refreshDrinkList()
        }
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

    fun setupEditTexts(view: View) {
        val startPicker: TextView? = view.findViewById(R.id.edit_start_time)
        val endPicker: TextView? = view.findViewById(R.id.edit_end_time)

        startPicker?.text = mConverter.timeToString(mMainActivity.startTimeMin, mMainActivity.use24HourTime)
        endPicker?.text = mConverter.timeToString(mMainActivity.endTimeMin, mMainActivity.use24HourTime)

        startPicker?.let { editText ->
            editText.setOnClickListener {
                val currentTime = Calendar.getInstance()
                var hour = currentTime.get(Calendar.HOUR_OF_DAY)
                var minute = currentTime.get(Calendar.MINUTE)
                if (mMainActivity.startTimeMin != -1) {
                    hour = mMainActivity.startTimeMin / 60
                    minute = mMainActivity.startTimeMin % 60
                }
                val mTimePicker: TimePickerDialog
                mTimePicker = TimePickerDialog(
                    ContextThemeWrapper(context!!, getDialogTheme()),
                    TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                        editText.text = mConverter.timeToString(selectedHour, selectedMinute, mMainActivity.use24HourTime)
                        val newMin = mConverter.militaryHoursAndMinutesToMinutes(selectedHour, selectedMinute)
                        mMainActivity.setPreference(startTimeMin = newMin)
                        if (mMainActivity.endTimeMin == -1) {
                            mMainActivity.setPreference(endTimeMin = newMin)
                        }
                        _timeSettings.tryEmit(_timeSettings.value.copy(startTimeMin = newMin))
                        val drinks = _drinks.value
                        updateBACDisplay(calculateBACInternal(drinks))
                    }, hour, minute, mMainActivity.use24HourTime
                )
                mTimePicker.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ ->
                    val nowMin = Constants.getCurrentTimeInMinuets()
                    mMainActivity.setPreference(startTimeMin = nowMin)
                    editText.text = mConverter.timeToString(nowMin, mMainActivity.use24HourTime)
                    _timeSettings.tryEmit(_timeSettings.value.copy(startTimeMin = nowMin))
                    val drinks = _drinks.value
                    updateBACDisplay(calculateBACInternal(drinks))
                }
                mTimePicker.setTitle("Start Time")
                mTimePicker.show()
            }
        }

        endPicker?.let { editText ->
            editText.setOnClickListener {
                val currentTime = Calendar.getInstance()
                var hour = currentTime.get(Calendar.HOUR_OF_DAY)
                var minute = currentTime.get(Calendar.MINUTE)
                if (mMainActivity.endTimeMin != -1) {
                    hour = mMainActivity.endTimeMin / 60
                    minute = mMainActivity.endTimeMin % 60
                }
                val mTimePicker: TimePickerDialog
                mTimePicker = TimePickerDialog(
                    ContextThemeWrapper(context!!, getDialogTheme()),
                    TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                        editText.text = mConverter.timeToString(selectedHour, selectedMinute, mMainActivity.use24HourTime)
                        val newMin = mConverter.militaryHoursAndMinutesToMinutes(selectedHour, selectedMinute)
                        mMainActivity.setPreference(endTimeMin = newMin)
                        _timeSettings.tryEmit(_timeSettings.value.copy(endTimeMin = newMin))
                        val drinks = _drinks.value
                        updateBACDisplay(calculateBACInternal(drinks))
                    }, hour, minute, mMainActivity.use24HourTime
                )
                mTimePicker.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ ->
                    val nowMin = Constants.getCurrentTimeInMinuets()
                    mMainActivity.setPreference(endTimeMin = nowMin)
                    editText.text = mConverter.timeToString(nowMin, mMainActivity.use24HourTime)
                    _timeSettings.tryEmit(_timeSettings.value.copy(endTimeMin = nowMin))
                    val drinks = _drinks.value
                    updateBACDisplay(calculateBACInternal(drinks))
                }
                mTimePicker.setTitle("End Time")
                mTimePicker.show()
            }
        }
    }

    private fun getDialogTheme(): Int {
        val converter = Converter()
        return converter.appThemeToDialogTheme[mMainActivity.activeTheme]
    }

    fun showOrHideEmptyListText(view: View) {
        // Compose handles empty state internally, this is kept for legacy compatibility
    }

    private fun calculateBACInternal(drinks: List<Drink>): Double {
        val drinksForCalc = mMainActivity.mDrinksList.map {
            BacCalculator.Drink(mConverter.drinkVolumeToFluidOz(it.amount, it.measurement), it.abv)
        }
        standardDrinksConsumed = mConverter.fluidOzToGrams(BacCalculator.alcoholOz(drinksForCalc)) / 14.0
        drinkingDuration = BacCalculator.hoursElapsed(mMainActivity.startTimeMin, mMainActivity.endTimeMin)

        val weightInLbs = mConverter.weightToLbs(mMainActivity.weight, mMainActivity.weightMeasurement)
        bac = BacCalculator.calculate(
            drinksForCalc, weightInLbs, mMainActivity.sex!!,
            mMainActivity.startTimeMin, mMainActivity.endTimeMin
        )

        mMainActivity.sendActionToBacNotificationService(Constants.ACTION.UPDATE_NOTIFICATION)
        return bac
    }

    fun calculateBAC(): Double {
        val drinks = _drinks.value
        return calculateBACInternal(drinks)
    }

    fun updateBACText(update: Double) {
        bac = update
    }

    fun clearSession() {
        mMainActivity.mDrinksList.clear()
        lifecycleScope.launch { mMainActivity.repository.clearCurrentSession() }
        _drinks.tryEmit(emptyList())
        _bacValue.tryEmit(0.0)
        _bacState.tryEmit(BacState.Sober)
        showOrHideEmptyListText(view ?: return)
        mMainActivity.resetTime()
        view?.let { setupEditTexts(it) }
        mMainActivity.sendActionToBacNotificationService(Constants.ACTION.STOP_SERVICE)

        // Notify BacNotificationService via HomeFragment legacy interface
        updateBACText(0.0)
    }

    private fun showDisclaimerDialog() {
        val dialog = SimpleDialog(context!!, layoutInflater)
        dialog.setTitle(getString(R.string.disclaimer))
        dialog.setBody(getString(R.string.disclaimer_body))
        dialog.setNeutralFunction { dialog.dismiss() }
    }

    private fun updateBACDisplay(bacVal: Double) {
        _bacValue.tryEmit(bacVal)
        _bacState.tryEmit(determineBacState(bacVal))
        this.bac = bacVal

        // Also update legacy views if they exist (Compose handles UI reactively too)
        val text = "%.3f".format(bacVal)
        val colorRes = when {
            bacVal >= 0.4 -> R.color.colorBlack
            bacVal >= 0.2 -> R.color.colorBlack
            bacVal > 0.12 -> R.color.colorRed
            bacVal > 0.07 -> R.color.colorOrange
            bacVal > 0.04 -> R.color.colorLighterGreen
            else -> R.color.colorGreen
        }

        val statusText = when {
            bacVal >= 0.4 -> "Dead"
            bacVal >= 0.2 -> "In Danger"
            bacVal > 0.12 -> "Shit Faced"
            bacVal > 0.07 -> "Drunk"
            bacVal > 0.04 -> "Tipsy"
            else -> "Sober"
        }

        view?.findViewById<TextView>(R.id.text_home_bac_value)?.text = text
        view?.findViewById<TextView>(R.id.text_home_bac_result)?.text = statusText
    }

    private fun determineBacState(bac: Double): BacState {
        return when {
            bac >= 0.4 -> BacState.Dead
            bac >= 0.2 -> BacState.InDanger
            bac > 0.12 -> BacState.ShitFaced
            bac > 0.07 -> BacState.Drunk
            bac > 0.04 -> BacState.Tipsy
            else -> BacState.Sober
        }
    }
}
