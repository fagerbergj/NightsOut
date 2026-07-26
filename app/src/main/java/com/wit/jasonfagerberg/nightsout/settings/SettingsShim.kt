package com.wit.jasonfagerberg.nightsout.settings

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// TEMPORARY BRIDGE for issue #53: SharedPreferences-shaped blocking facade over DataStore,
// so call sites migrate with no behavior change. Issue #54 (ViewModel migration) consumes
// SettingsRepository Flows directly and DELETES this class. Do not add new usages.
@Deprecated("Temporary bridge — removed by #54 ViewModel migration")
class SettingsShim(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    fun getBoolean(key: String, defValue: Boolean): Boolean = runBlocking {
        dataStore.data.first()[booleanPreferencesKey(key)] ?: defValue
    }

    fun getInt(key: String, defValue: Int): Int = runBlocking {
        dataStore.data.first()[intPreferencesKey(key)] ?: defValue
    }

    fun getLong(key: String, defValue: Long): Long = runBlocking {
        dataStore.data.first()[longPreferencesKey(key)] ?: defValue
    }

    fun getFloat(key: String, defValue: Float): Float = runBlocking {
        dataStore.data.first()[floatPreferencesKey(key)] ?: defValue
    }

    fun getString(key: String, defValue: String?): String? = runBlocking {
        dataStore.data.first()[stringPreferencesKey(key)] ?: defValue
    }

    fun edit(): Editor = Editor()

    // Mirrors SharedPreferences.Editor: stage puts, persist in one DataStore transaction.
    inner class Editor {
        private val staged = mutableMapOf<Preferences.Key<*>, Any>()

        fun putBoolean(key: String, value: Boolean) = apply { staged[booleanPreferencesKey(key)] = value }
        fun putInt(key: String, value: Int) = apply { staged[intPreferencesKey(key)] = value }
        fun putLong(key: String, value: Long) = apply { staged[longPreferencesKey(key)] = value }
        fun putFloat(key: String, value: Float) = apply { staged[floatPreferencesKey(key)] = value }

        // null is a no-op: DataStore can't store nulls and no call site ever writes one
        fun putString(key: String, value: String?) = apply {
            if (value != null) staged[stringPreferencesKey(key)] = value
        }

        fun apply() = runBlocking {
            dataStore.edit { prefs -> staged.forEach { (key, value) -> prefs.putUnchecked(key, value) } }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun MutablePreferences.putUnchecked(key: Preferences.Key<*>, value: Any) {
    this[key as Preferences.Key<Any>] = value
}
