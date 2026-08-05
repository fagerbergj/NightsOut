package com.wit.jasonfagerberg.nightsout.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.domain.BacCalculator
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.utils.Converter
 import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TimeSettings(
    val startTimeMin: Int,
    val endTimeMin: Int,
    val use24HourTime: Boolean
)

sealed interface BacState {
    data object Sober : BacState
    data object Tipsy : BacState
    data object Drunk : BacState
    data object ShitFaced : BacState
    data object InDanger : BacState
    data object Dead : BacState
}

data class HomeUiState(
    val drinks: List<Drink> = emptyList(),
    val bacState: BacState = BacState.Sober,
    val bacValue: Double = 0.0,
    val timeSettings: TimeSettings = TimeSettings(-1, -1, false),
    val standardDrinksConsumed: Double = 0.0,
    val drinkingDuration: Double = 0.0,
    // set once if the initial repository load throws; surfaced as a toast, never cleared back
    val loadError: Boolean = false
)

class HomeViewModel(
    private val repository: NightsOutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    val sex: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val weight: MutableStateFlow<Double> = MutableStateFlow(0.0)
    val weightMeasurement: MutableStateFlow<String> = MutableStateFlow("lbs")

    private val _initialLoadComplete = MutableStateFlow(false)
    val initialLoadComplete: StateFlow<Boolean> = _initialLoadComplete

    init {
        viewModelScope.launch {
            try {
                applyLoadedDrinks(repository.pullCurrentSessionDrinks())
                _existingHeaders = repository.pullLogHeaders().toMutableList()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(loadError = true)
            } finally {
                _initialLoadComplete.value = true
            }
        }
    }

    // shared by init (repository load) and addCurrentSessionDrinks (manual refresh) - one emit, not one per drink
    private fun applyLoadedDrinks(drinks: List<Drink>) {
        val newBac = calculateBACInternal(drinks)
        _uiState.value = _uiState.value.copy(
            drinks = drinks,
            bacValue = newBac,
            bacState = determineBacState(newBac),
            standardDrinksConsumed = calcStandardDrinks(drinks),
            drinkingDuration = calcDuration()
        )
    }

    fun addDrink(drink: Drink) {
        viewModelScope.launch {
            val current = _uiState.value
            val newDrinks = current.drinks + drink
            val newBac = calculateBACInternal(newDrinks)
            _uiState.value = current.copy(
                drinks = newDrinks,
                bacValue = newBac,
                bacState = determineBacState(newBac)
            )
        }
    }

    fun deleteDrinkAt(position: Int) {
        viewModelScope.launch {
            val current = _uiState.value
            if (position >= current.drinks.size) return@launch
            repository.deleteCurrentSessionAt(position)
            val newDrinks = current.drinks.toMutableList().apply { removeAt(position) }
            val newBac = calculateBACInternal(newDrinks)
            _uiState.value = current.copy(
                drinks = newDrinks,
                bacValue = newBac,
                bacState = determineBacState(newBac)
            )
        }
    }

    fun updateTimeSettings(
        startTimeMin: Int,
        endTimeMin: Int,
        use24HourTime: Boolean = _uiState.value.timeSettings.use24HourTime
    ) {
        viewModelScope.launch {
            val current = _uiState.value
            val newBac = calculateBACInternal(current.drinks)
            _uiState.value = current.copy(
                timeSettings = TimeSettings(startTimeMin, endTimeMin, use24HourTime),
                bacValue = newBac,
                bacState = determineBacState(newBac)
            )
        }
    }

    fun updateProfile(sex: Boolean, weight: Double, weightMeasurement: String) {
        viewModelScope.launch {
            this@HomeViewModel.sex.value = sex
            this@HomeViewModel.weight.value = weight
            this@HomeViewModel.weightMeasurement.value = weightMeasurement
            val current = _uiState.value
            val newBac = calculateBACInternal(current.drinks)
            _uiState.value = current.copy(
                bacValue = newBac,
                bacState = determineBacState(newBac)
            )
        }
    }

    fun addCurrentSessionDrinks(drinks: List<Drink>) {
        viewModelScope.launch { applyLoadedDrinks(drinks.toList()) }
    }

    fun recalibrate() {
        viewModelScope.launch {
            val current = _uiState.value
            val newBac = calculateBACInternal(current.drinks)
            _uiState.value = current.copy(
                bacValue = newBac,
                bacState = determineBacState(newBac)
            )
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            repository.clearCurrentSession()
            _uiState.value = HomeUiState(
                drinks = emptyList(),
                bacValue = 0.0,
                bacState = BacState.Sober
            )
        }
    }

    fun onAddDrinkClicked(context: Context) {
        val activity = context as? android.app.Activity ?: return
        val intent = Intent(activity, AddDrinkActivity::class.java)
        intent.putExtra("CAN_UNFAVORITE", true)
        intent.putExtra("FAVORITED", false)
        activity.startActivity(intent)
    }

    fun onFavoriteClicked(drink: Drink) {
        val wasFavorited = drink.favorited
        viewModelScope.launch {
            if (!wasFavorited) {
                repository.insertRowInFavoritesTable(drink.name, drink.id)
            } else {
                repository.deleteFavoriteByName(drink.name)
            }
            // only flip once the write succeeds - a failed write must leave the UI matching the DB
            drink.favorited = !wasFavorited
        }
    }

    fun showDisclaimer(context: Context) {
        val activity = context as? android.app.Activity ?: return
        val dialog = SimpleDialog(activity, activity.layoutInflater)
        dialog.setTitle(activity.getString(R.string.disclaimer))
        dialog.setBody(activity.getString(R.string.disclaimer_body))
        dialog.setNeutralFunction { dialog.dismiss() }
    }

    fun calculateBACValue(): Double = calculateBACInternal(_uiState.value.drinks)
    fun getStandardDrinksConsumed(): Double = _uiState.value.standardDrinksConsumed
    fun getDrinkingDuration(): Double = _uiState.value.drinkingDuration
   fun getConverter(): Converter = Converter()

    sealed interface LogAction {
        data class UpdateExistingLog(val logDate: Int, val existingBac: Double, val existingDuration: Double) : LogAction
        data object CreateNewLog : LogAction
    }

    fun checkOrCreateLog(logDate: Int): LogAction? {
        if (!_initialLoadComplete.value) return null
        val existingHeader = _existingHeaders.find { it.date == logDate }
        return if (existingHeader != null) {
            LogAction.UpdateExistingLog(logDate, existingHeader.bac, existingHeader.duration)
        } else {
            LogAction.CreateNewLog
        }
    }

    suspend fun logSessionWithDate(logDate: Int): Boolean {
        _initialLoadComplete.first { it }
        val current = _uiState.value
        if (current.drinks.isEmpty()) return false
        
        val result = runCatching {
            val existing = _existingHeaders.find { it.date == logDate }
            if (existing != null) {
                repository.deleteLog(existing.date)
                repository.insertRowInLogTable(logDate, current.bacValue, current.drinkingDuration)
                repository.pushDrinksToLogDrinks(logDate, current.drinks)
                _existingHeaders = _existingHeaders.map { if (it.date == existing.date) com.wit.jasonfagerberg.nightsout.models.LogHeader(logDate, current.bacValue, current.drinkingDuration) else it }.toMutableList()
            } else {
                repository.insertRowInLogTable(logDate, current.bacValue, current.drinkingDuration)
                repository.pushDrinksToLogDrinks(logDate, current.drinks)
                _existingHeaders.add(com.wit.jasonfagerberg.nightsout.models.LogHeader(logDate, current.bacValue, current.drinkingDuration))
            }
        }
        if (result.isSuccess) clearSession()
        return result.isSuccess
    }

    fun getExistingHeaders(): List<com.wit.jasonfagerberg.nightsout.models.LogHeader> = _existingHeaders.toList()

 private var _existingHeaders: MutableList<com.wit.jasonfagerberg.nightsout.models.LogHeader> = ArrayList()

    private fun calcStandardDrinks(drinks: List<Drink>): Double {
        val converter = Converter()
        val bacDrinks = drinks.map {
            BacCalculator.Drink(converter.drinkVolumeToFluidOz(it.amount, it.measurement), it.abv)
        }
        return converter.fluidOzToGrams(BacCalculator.alcoholOz(bacDrinks)) / 14.0
    }

    private fun calcDuration(): Double {
        val current = _uiState.value
        return BacCalculator.hoursElapsed(current.timeSettings.startTimeMin, current.timeSettings.endTimeMin)
    }

    private fun calculateBACInternal(drinks: List<Drink>): Double {
        val converter = Converter()
        val bacDrinks = drinks.map {
            BacCalculator.Drink(converter.drinkVolumeToFluidOz(it.amount, it.measurement), it.abv)
        }
        val weightInLbs = converter.weightToLbs(weight.value, weightMeasurement.value)
        return BacCalculator.calculate(
            bacDrinks, weightInLbs, sex.value,
            _uiState.value.timeSettings.startTimeMin,
            _uiState.value.timeSettings.endTimeMin
        )
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
