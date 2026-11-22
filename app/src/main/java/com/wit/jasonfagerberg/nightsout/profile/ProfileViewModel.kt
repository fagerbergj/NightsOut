package com.wit.jasonfagerberg.nightsout.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wit.jasonfagerberg.nightsout.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val sex: Boolean? = null,
    val weightText: String = "",
    val weightMeasurement: String = "kg"
)

class ProfileViewModel(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private companion object {
        const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { settingsRepo.profileSex.collect { v -> _uiState.value = _uiState.value.copy(sex = v) } }
        viewModelScope.launch { settingsRepo.profileWeight.collect { w -> _uiState.value = _uiState.value.copy(weightText = w.toString()) } }
        viewModelScope.launch { settingsRepo.profileWeightMeasurement.collect { m -> if (m in listOf("kg", "lbs")) _uiState.value = _uiState.value.copy(weightMeasurement = m) } }
    }

    fun setSex(isMale: Boolean) { _uiState.value = _uiState.value.copy(sex = isMale) }
    fun setWeightText(text: String) { _uiState.value = _uiState.value.copy(weightText = text) }
    fun setWeightMeasurement(m: String) { _uiState.value = _uiState.value.copy(weightMeasurement = m) }

    fun saveProfile(context: android.content.Context) {
        viewModelScope.launch {
            when (val result = doSave()) {
                is SaveResult.Success -> context.showToast("Profile Saved!")
                is SaveResult.Invalid -> context.showToast("Please fill in all fields", true)
                is SaveResult.Error -> {
                    Log.e(TAG, "Failed to save profile", result.error)
                    context.showToast("Couldn't Save Profile", true)
                }
            }
        }
    }

    private sealed interface SaveResult {
        data object Success : SaveResult
        data object Invalid : SaveResult
        data class Error(val error: Throwable) : SaveResult
    }

    private suspend fun doSave(): SaveResult {
        val uis = _uiState.value
        val sex = uis.sex
        val weightStr = uis.weightText
        if (sex == null || weightStr.isEmpty()) return SaveResult.Invalid
        val w = try { weightStr.toDouble() } catch (_: Exception) { return SaveResult.Invalid }
        if (w < 20) return SaveResult.Invalid
        return try {
            settingsRepo.setProfileInit(true)
            settingsRepo.setProfileSex(sex)
            settingsRepo.setProfileWeight(w.toFloat())
            settingsRepo.setProfileWeightMeasurement(uis.weightMeasurement)
            SaveResult.Success
        } catch (e: Exception) { SaveResult.Error(e) }
    }

    fun hasUnsavedData(currentSex: Boolean?, currentWeight: Double, currentMeas: String): Boolean =
        with(_uiState.value) {
            val sameSex = sex == null || sex == currentSex
            val sameWeight = try { weightText.toDoubleOrNull()?.let { Math.abs(it - currentWeight) <= 0.01 } ?: true } catch (_: Exception) { true }
            sameSex && sameWeight && (weightMeasurement == currentMeas)
        }.not()
}
