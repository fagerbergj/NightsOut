package com.wit.jasonfagerberg.nightsout.addDrink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.utils.Converter
import java.text.DecimalFormat
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddDrinkUiState(
    val searchResults: List<Drink> = emptyList(),
    val favorites: List<Drink> = emptyList(),
    val recents: List<Drink> = emptyList(),
    val name: String = "",
    val abvText: String = "",
    val amountText: String = "",
    val measurement: String = "oz",
    val complexMode: Boolean = false,
    val favorited: Boolean = false,
    val inputErrors: List<String> = emptyList(),
    val calculatedABV: Double = Double.NaN
)

data class AlcoholSource(val abv: Double, val amount: Double, val measurement: String) {
    override fun equals(other: Any?): Boolean {
        val o = other as AlcoholSource
        return this.abv == o.abv && this.amount == o.amount && this.measurement == o.measurement
    }

    override fun hashCode(): Int {
        return abv.hashCode() + amount.hashCode() + measurement.hashCode()
    }
}

class AddDrinkViewModel(
    private val repository: NightsOutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddDrinkUiState())
    val uiState: StateFlow<AddDrinkUiState> = _uiState.asStateFlow()

    private val _complexIngredients = MutableStateFlow<List<AlcoholSource>>(emptyList())
    val complexIngredients: StateFlow<List<AlcoholSource>> = _complexIngredients.asStateFlow()

    private var _searchJob: Job? = null
    private val converter = Converter()

    var canUnfavorite: Boolean = true

    // Callbacks for UI-side side effects (toast, navigation)
    var onDrinkAdded: (() -> Unit)? = null
    var onErrorToast: ((String) -> Unit)? = null
    var onNavigateBack: (() -> Unit)? = null

    fun navigateBack() {
        onNavigateBack?.invoke()
    }

    fun submitDrinkAsync() {
        viewModelScope.launch {
            submitDrink()
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val favs = repository.pullFavoriteDrinks()
            _uiState.value = _uiState.value.copy(favorites = favs)
        }
        viewModelScope.launch {
            val recents = repository.pullRecentDrinks()
            _uiState.value = _uiState.value.copy(recents = recents)
        }
    }

    fun onSearchChanged(filter: String) {
        _searchJob?.cancel()
        _searchJob = viewModelScope.launch {
            val results = repository.getSuggestedDrinks(filter)
            _uiState.value = _uiState.value.copy(searchResults = results)
        }
    }

    fun onDrinkSelected(drink: Drink) {
        _uiState.value = _uiState.value.copy(
            name = drink.name,
            abvText = drink.abv.toString(),
            amountText = drink.amount.toString(),
            measurement = drink.measurement.ifEmpty { "oz" }
        )
    }

    fun setFormFields(name: String, abv: Double, amount: Double, measurement: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            abvText = abv.toString(),
            amountText = amount.toString(),
            measurement = measurement.ifEmpty { "oz" }
        )
    }

    fun onComplexModeChanged(complex: Boolean) {
        _uiState.value = _uiState.value.copy(complexMode = complex)
        if (!complex) {
            _complexIngredients.value = emptyList()
            _uiState.value = _uiState.value.copy(calculatedABV = Double.NaN)
        }
    }

    fun onNameChanged(text: String) {
        _uiState.value = _uiState.value.copy(name = text, inputErrors = emptyList())
    }

    fun onAbvChanged(text: String) {
        _uiState.value = _uiState.value.copy(abvText = text, inputErrors = emptyList())
    }

    fun onAmountChanged(text: String) {
        _uiState.value = _uiState.value.copy(amountText = text, inputErrors = emptyList())
    }

    fun onMeasurementChanged(measurement: String) {
        _uiState.value = _uiState.value.copy(measurement = measurement, inputErrors = emptyList())
    }

    fun setFavorited(favorite: Boolean) {
        _uiState.value = _uiState.value.copy(favorited = favorite)
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(favorited = !_uiState.value.favorited)
    }

    // Validation returns list of error field names; updates internal state too
    fun validateInput(): List<String> {
        val nameText = _uiState.value.name.trim()
        val abvRaw = _uiState.value.abvText.trim()
        val amountRaw = _uiState.value.amountText.trim()
        val measurement = _uiState.value.measurement

        val abv = converter.stringToDouble(abvRaw)
        val amount = converter.stringToDouble(amountRaw)

        // Normalize if valid
        if (!abv.isNaN()) {
            _uiState.value = _uiState.value.copy(abvText = abv.toString())
        }
        if (!amount.isNaN()) {
            _uiState.value = _uiState.value.copy(amountText = amount.toString())
        }

        val errors = mutableListOf<String>()
        val foz = converter.drinkVolumeToFluidOz(amount, measurement)
        if (amount.isNaN() || foz > 560) errors += "amount"
        if (abv.isNaN() || abv > 100.0) errors += "abv"
        if (nameText.isEmpty()) errors += "name"

        _uiState.value = _uiState.value.copy(inputErrors = errors)
        return errors
    }

    fun setVolumeMeasurementLocale() {
        val country = java.util.Locale.getDefault().country
        val defaultMeasurement = if (country == "US" || country == "LR" || country == "MM") "oz" else "ml"
        if (_uiState.value.measurement !in listOf("oz", "ml")) {
            _uiState.value = _uiState.value.copy(measurement = defaultMeasurement)
        }
    }

    fun clearFavoritesAndDB(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllFavorites()
            _uiState.value = _uiState.value.copy(favorites = emptyList(), inputErrors = emptyList())
            onComplete()
        }
    }

    fun clearRecentsAndDB(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecentDrinks()
            _uiState.value = _uiState.value.copy(recents = emptyList(), inputErrors = emptyList())
            onComplete()
        }
    }

    // Complex drink helpers

    fun addAlcoholSource() {
        val abv = converter.stringToDouble(_uiState.value.abvText.trim())
        val amount = converter.stringToDouble(_uiState.value.amountText.trim())
        val measurement = _uiState.value.measurement

        if (abv.isNaN() || amount.isNaN()) return
        val source = AlcoholSource(abv, amount, measurement)
        _complexIngredients.value = _complexIngredients.value + source
        _uiState.value = _uiState.value.copy(abvText = "", amountText = "")
    }

    fun removeAlcoholSourceAt(index: Int) {
        _complexIngredients.value = _complexIngredients.value.toMutableList().apply { removeAt(index) }
    }

    fun getAlcoholSourceAt(index: Int): AlcoholSource? = _complexIngredients.value.getOrNull(index)

    fun complexIngredientCount(): Int = _complexIngredients.value.size

    fun sumAmount(): Double {
        var sum = 0.0
        for (source in _complexIngredients.value) {
            sum += converter.drinkVolumeToFluidOz(source.amount, source.measurement)
        }
        return if (_complexIngredients.value.isEmpty()) Double.NaN else sum
    }

    fun weightedAverageAbv(): Double {
        val sources = _complexIngredients.value
        if (sources.isEmpty()) return Double.NaN
        val sum = sumAmount()
        if (sum == 0.0 && sources.isNotEmpty()) return sources.size.toDouble()
        var ave = 0.0
        for (source in sources) {
            val weight = converter.drinkVolumeToFluidOz(source.amount, source.measurement) / sum
            ave += source.abv * weight
        }
        val twoDForm = DecimalFormat("#.##")
        return java.lang.Double.valueOf(twoDForm.format(ave))
    }

    // Drink submission - uses `this.canUnfavorite` from ViewModel config
    suspend fun submitDrink(): Boolean {
        val state = _uiState.value
        val errors = if (!state.complexMode) validateInput() else listOf<String>()
        if (errors.isNotEmpty()) {
            onErrorToast?.invoke("Please enter a valid input")
            return false
        }
        if (state.complexMode && complexIngredientCount() == 0) {
            val formErrors = validateInput()
            if (formErrors.isNotEmpty()) {
                onErrorToast?.invoke("Please enter a valid input")
                return false
            }
        }

        val name = state.name.trim()
        val abv = if (!state.complexMode) state.abvText.toDoubleOrNull() ?: 0.0 else weightedAverageAbv()
        val amount = if (!state.complexMode) state.amountText.toDoubleOrNull() ?: 0.0 else sumAmount()
        val measurement = if (!state.complexMode) state.measurement else "oz"

        val drink = Drink(
            UUID.randomUUID(), name, abv, amount, measurement,
            state.favorited, true, Constants.getLongTimeNow()
        )

        val id = repository.getDrinkIdFromFullDrinkInfo(drink)
        drink.id = id
        if (!repository.idInDb(id)) repository.insertDrinkIntoDrinksTable(drink)
        repository.updateDrinkModifiedTime(drink.id, drink.modifiedTime)
        repository.updateDrinkSuggestionStatus(drink.id, false)
        val wasFavorited = repository.isFavoritedInDB(drink.name) || drink.favorited
        drink.favorited = wasFavorited

        if (canUnfavorite) {
            repository.insertRowInCurrentSessionTable(drink.id, repository.currentSessionCount())
            repository.setRecentByName(drink.name, false)
            repository.setRecentById(drink.id, true)
            if (drink.favorited) {
                repository.updateDrinkFavoriteStatus(drink)
            }
        } else {
            drink.recent = false
            repository.updateDrinkFavoriteStatus(drink)
        }

        // Reset form
        setFormFields("", 0.0, 0.0, "oz")
        onComplexModeChanged(false)
        _complexIngredients.value = emptyList()

        onDrinkAdded?.invoke()
        return true
    }
}
