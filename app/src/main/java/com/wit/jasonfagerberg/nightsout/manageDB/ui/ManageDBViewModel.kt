package com.wit.jasonfagerberg.nightsout.manageDB.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.models.Drink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ManageDBViewModel(
    private val repository: NightsOutRepository
) : ViewModel() {

    private val _allDrinks = MutableStateFlow<List<Drink>>(emptyList())

    // Filtered list displayed to the user - derived from search query + full list
    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            try {
                _allDrinks.value = repository.getSuggestedDrinks("", true).toList()
            } catch (_: Exception) { /* keep empty, toast shown at UI layer */ }
        }
    }

    val filteredDrinks: StateFlow<List<Drink>> = combine(_searchQuery, _allDrinks) { query, drinks ->
        if (query.isEmpty()) drinks else drinks.filter { it.name.contains(query, ignoreCase = true) }
    }

    val searchQuery: StateFlow<String> get() = _searchQuery

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(drink: Drink) {
        drink.favorited = !drink.favorited
        viewModelScope.launch {
            repository.updateDrinkModifiedTime(drink.id, Constants.getLongTimeNow())
            repository.updateDrinkFavoriteStatus(drink)
        }
    }

    fun deleteDrink(drink: Drink) {
        val current = _allDrinks.value.toMutableList()
        current.removeAll { it == drink }
        _allDrinks.value = current
        viewModelScope.launch {
            // Remove current session reference (matching adapter's removeCurrentSessionReference)
            repository.deleteCurrentSessionByDrinkId(drink.id)
            repository.deleteDrinkById(drink.id)
            // Update favorites table consistency
            repository.updateDrinkFavoriteStatus(drink)
        }
    }

    /**
     * Builds the reference-loss string for a drink.
     * Matches the adapter's getLostReferenceString() logic verbatim, checking 4 categories:
     * 1) Current Drinks List (current session)
     * 2) Favorite Drink Reference
     * 3) Recent Drink Reference
     * 4) Logged Drink Reference
     */
    suspend fun getLostReferenceString(drink: Drink): String {
        var loss = ""

        for (d in repository.pullCurrentSessionDrinks()) {
            if (d.isExactDrink(drink)) {
                loss += "Drink in Current Drinks List\n"
                break
            }
        }
        for (f in repository.pullFavoriteDrinks()) {
            if (f.isExactDrink(drink)) {
                loss += "Favorite Drink Reference\n"
                break
            }
        }
        for (r in repository.pullRecentDrinks()) {
            if (r.isExactDrink(drink)) {
                loss += "Recent Drink Reference\n"
                break
            }
        }

        if (repository.isLoggedDrink(drink.id)) loss += "Logged Drink Reference"

        return loss
    }

    fun toggleSuggestion(drink: Drink) {
        viewModelScope.launch {
            val dontSuggest = repository.getDrinkSuggestedStatus(drink.id)
            repository.updateDrinkSuggestionStatus(drink.id, !dontSuggest)
        }
    }

    fun cleanDatabase() {
        viewModelScope.launch {
            val currentDrinks = _allDrinks.value.toList()
            val toRemove = mutableSetOf<Drink>()
            for (drink in currentDrinks) {
                if (getLostReferenceString(drink).isEmpty()) {
                    toRemove.add(drink)
                }
            }
            _allDrinks.value = _allDrinks.value.filterNot { it in toRemove }
            for (drink in toRemove) repository.deleteDrinkById(drink.id)
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            repository.resetDatabase()
            val refreshed = repository.getSuggestedDrinks("", true).toList()
            _allDrinks.value = if (_searchQuery.value.isEmpty()) refreshed
            else filteredDrinks.value.filter { it in refreshed.toSet() }
        }
    }
}
