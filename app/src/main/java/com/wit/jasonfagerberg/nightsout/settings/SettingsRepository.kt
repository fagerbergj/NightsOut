package com.wit.jasonfagerberg.nightsout.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.wit.jasonfagerberg.nightsout.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Process-wide single DataStore (the delegate caches by name). The migration name is the
// default SharedPreferences file (PreferenceManager's "<package>_preferences"), so existing
// users keep their settings on first access.
internal val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "${context.packageName}_preferences"))
    }
)

// The real settings API: Flow reads and suspend writes per key. #54 (ViewModels) consumes
// this directly; until then call sites go through the blocking SettingsShim bridge.
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val profileInit: Flow<Boolean> = read(SettingsKeys.PROFILE_INIT, false)
    val dateInstalled: Flow<Long> = read(SettingsKeys.DATE_INSTALLED, 0L)
    val drinksAddedCount: Flow<Int> = read(SettingsKeys.DRINKS_ADDED_COUNT, 0)
    val dontShowRateDialog: Flow<Boolean> = read(SettingsKeys.DONT_SHOW_RATE_DIALOG, false)
    val dontShowBacNotification: Flow<Boolean> = read(SettingsKeys.DONT_SHOW_BAC_NOTIFICATION, false)
    val showBacNotification: Flow<Boolean> = read(SettingsKeys.SHOW_BAC_NOTIFICATION, true)
    val activeTheme: Flow<Int> = read(SettingsKeys.ACTIVE_THEME, R.style.AppTheme)
    val profileSex: Flow<Boolean> = read(SettingsKeys.PROFILE_SEX, true)
    val profileWeight: Flow<Float> = read(SettingsKeys.PROFILE_WEIGHT, 0f)
    val profileWeightMeasurement: Flow<String> = read(SettingsKeys.PROFILE_WEIGHT_MEASUREMENT, "")
    val use24HourTime: Flow<Boolean> = read(SettingsKeys.USE_24_HOUR_TIME, false)
    val startTimeMin: Flow<Int> = read(SettingsKeys.START_TIME, 0)
    val endTimeMin: Flow<Int> = read(SettingsKeys.END_TIME, 0)
    val isBacNotificationStarted: Flow<Boolean> = read(SettingsKeys.IS_BAC_NOTIFICATION_STARTED, false)

    suspend fun setProfileInit(value: Boolean) = write(SettingsKeys.PROFILE_INIT, value)
    suspend fun setDateInstalled(value: Long) = write(SettingsKeys.DATE_INSTALLED, value)
    suspend fun setDrinksAddedCount(value: Int) = write(SettingsKeys.DRINKS_ADDED_COUNT, value)
    suspend fun setDontShowRateDialog(value: Boolean) = write(SettingsKeys.DONT_SHOW_RATE_DIALOG, value)
    suspend fun setDontShowBacNotification(value: Boolean) = write(SettingsKeys.DONT_SHOW_BAC_NOTIFICATION, value)
    suspend fun setShowBacNotification(value: Boolean) = write(SettingsKeys.SHOW_BAC_NOTIFICATION, value)
    suspend fun setActiveTheme(value: Int) = write(SettingsKeys.ACTIVE_THEME, value)
    suspend fun setProfileSex(value: Boolean) = write(SettingsKeys.PROFILE_SEX, value)
    suspend fun setProfileWeight(value: Float) = write(SettingsKeys.PROFILE_WEIGHT, value)
    suspend fun setProfileWeightMeasurement(value: String) = write(SettingsKeys.PROFILE_WEIGHT_MEASUREMENT, value)
    suspend fun setUse24HourTime(value: Boolean) = write(SettingsKeys.USE_24_HOUR_TIME, value)
    suspend fun setStartTimeMin(value: Int) = write(SettingsKeys.START_TIME, value)
    suspend fun setEndTimeMin(value: Int) = write(SettingsKeys.END_TIME, value)
    suspend fun setIsBacNotificationStarted(value: Boolean) = write(SettingsKeys.IS_BAC_NOTIFICATION_STARTED, value)

    private fun <T> read(key: Preferences.Key<T>, default: T): Flow<T> =
        dataStore.data.map { it[key] ?: default }

    private suspend fun <T> write(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }
}
