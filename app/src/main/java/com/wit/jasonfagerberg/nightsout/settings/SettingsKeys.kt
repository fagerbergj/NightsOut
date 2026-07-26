package com.wit.jasonfagerberg.nightsout.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wit.jasonfagerberg.nightsout.constants.Constants

// Names intentionally match the legacy SharedPreferences keys so SharedPreferencesMigration
// carries existing users' values over unchanged.
object SettingsKeys {
    val PROFILE_INIT = booleanPreferencesKey(Constants.PREFERENCE.PROFILE_INIT)
    val DATE_INSTALLED = longPreferencesKey(Constants.PREFERENCE.DATE_INSTALLED)
    val DRINKS_ADDED_COUNT = intPreferencesKey(Constants.PREFERENCE.DRINKS_ADDED_COUNT)
    val DONT_SHOW_RATE_DIALOG = booleanPreferencesKey(Constants.PREFERENCE.DONT_SHOW_RATE_DIALOG)
    val DONT_SHOW_BAC_NOTIFICATION = booleanPreferencesKey(Constants.PREFERENCE.DONT_SHOW_BAC_NOTIFICATION)
    val SHOW_BAC_NOTIFICATION = booleanPreferencesKey(Constants.PREFERENCE.SHOW_BAC_NOTIFICATION)
    val ACTIVE_THEME = intPreferencesKey(Constants.PREFERENCE.ACTIVE_THEME)
    val PROFILE_SEX = booleanPreferencesKey(Constants.PREFERENCE.PROFILE_SEX)
    val PROFILE_WEIGHT = floatPreferencesKey(Constants.PREFERENCE.PROFILE_WEIGHT)
    val PROFILE_WEIGHT_MEASUREMENT = stringPreferencesKey(Constants.PREFERENCE.PROFILE_WEIGHT_MEASUREMENT)
    val USE_24_HOUR_TIME = booleanPreferencesKey(Constants.PREFERENCE.USE_24_HOUR_TIME)
    val START_TIME = intPreferencesKey(Constants.PREFERENCE.START_TIME)
    val END_TIME = intPreferencesKey(Constants.PREFERENCE.END_TIME)
    val IS_BAC_NOTIFICATION_STARTED = booleanPreferencesKey(Constants.PREFERENCE.IS_BAC_NOTIFICATION_STARTED)
}
