package com.wit.jasonfagerberg.nightsout.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wit.jasonfagerberg.nightsout.R
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    app: Application,
    private val repository: SettingsRepository
) : AndroidViewModel(app) {

    val showBacNotification: StateFlow<Boolean> = repository.showBacNotification
        .onStart { emit(false) }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val showDarkMode: StateFlow<Boolean> = repository.activeTheme
        .map { it == R.style.DarkAppTheme }
        .onStart { emit(false) }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val use24HourTime: StateFlow<Boolean> = repository.use24HourTime
        .onStart { emit(false) }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    suspend fun isProfileInit(): Boolean = repository.profileInit.first()

    fun toggleDarkTheme(toggledOn: Boolean) {
        val themeRes = if (toggledOn) R.style.DarkAppTheme else R.style.AppTheme
        viewModelScope.launch {
            repository.setActiveTheme(themeRes)
        }
    }
}
