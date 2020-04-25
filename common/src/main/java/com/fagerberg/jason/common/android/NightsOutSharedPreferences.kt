package com.fagerberg.jason.common.android

import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.fagerberg.jason.common.R
import com.fagerberg.jason.common.constants.ACTIVE_THEME
import com.fagerberg.jason.common.constants.DATE_INSTALLED
import com.fagerberg.jason.common.constants.DONT_SHOW_BAC_NOTIFICATION
import com.fagerberg.jason.common.constants.DONT_SHOW_RATE_DIALOG
import com.fagerberg.jason.common.constants.DRINKS_ADDED_COUNT
import com.fagerberg.jason.common.constants.END_TIME
import com.fagerberg.jason.common.constants.PROFILE_INIT
import com.fagerberg.jason.common.constants.PROFILE_SEX
import com.fagerberg.jason.common.constants.PROFILE_WEIGHT
import com.fagerberg.jason.common.constants.PROFILE_WEIGHT_MEASUREMENT
import com.fagerberg.jason.common.constants.SHOW_BAC_NOTIFICATION
import com.fagerberg.jason.common.constants.START_TIME
import com.fagerberg.jason.common.constants.USE_24_HOUR_TIME
import com.fagerberg.jason.common.models.WeightMeasurement
import com.fagerberg.jason.common.utils.getCurrentTimeInMinuets
import com.fagerberg.jason.common.utils.isCountryThatUsesLbs
import com.fagerberg.jason.common.utils.isCountryThatUses12HourTime

data class NightsOutSharedPreferences(
    private val preferenceManager: SharedPreferences,
    val profileInit: Boolean,
    val sex: Boolean?,
    val weight: Double,
    val weightMeasurement: WeightMeasurement,
    val startTimeMin: Int,
    val endTimeMin: Int,
    val use24HourTime: Boolean,
    val dateInstalled: Long,
    val drinksAddedCount: Int,
    val dontShowRateDialog: Boolean,
    val dontShowCurrentBacNotification: Boolean,
    val showBacNotification: Boolean,
    val activeTheme: Int
) {

    private val logTag = this::class.java.name

    fun update(
        profileInit: Boolean = this.profileInit,
        sex: Boolean? = this.sex,
        weight: Double = this.weight,
        weightMeasurement: WeightMeasurement = this.weightMeasurement,
        startTimeMin: Int = this.startTimeMin,
        endTimeMin: Int = this.endTimeMin,
        use24HourTime: Boolean = this.use24HourTime,
        dateInstalled: Long = this.dateInstalled,
        drinksAddedCount: Int = this.drinksAddedCount,
        dontShowRateDialog: Boolean = this.dontShowRateDialog,
        dontShowCurrentBacNotification: Boolean = this.dontShowCurrentBacNotification,
        showBacNotification: Boolean = this.showBacNotification,
        activeTheme: Int = this.activeTheme
    ): NightsOutSharedPreferences {
        val editor = preferenceManager.edit()

        editor.putBoolean(PROFILE_INIT, true)
        if (sex != null) editor.putBoolean(PROFILE_SEX, sex)

        editor.putFloat(PROFILE_WEIGHT, weight.toFloat())
        if (weightMeasurement != null) editor.putString(PROFILE_WEIGHT_MEASUREMENT, weightMeasurement.displayName)

        editor.putInt(START_TIME, startTimeMin)
        editor.putInt(END_TIME, endTimeMin)
        editor.putBoolean(USE_24_HOUR_TIME, use24HourTime)

        editor.putLong(DATE_INSTALLED, dateInstalled)
        editor.putInt(DRINKS_ADDED_COUNT, drinksAddedCount)
        editor.putBoolean(DONT_SHOW_RATE_DIALOG, dontShowRateDialog)
        editor.putBoolean(DONT_SHOW_BAC_NOTIFICATION, dontShowCurrentBacNotification)
        editor.putBoolean(SHOW_BAC_NOTIFICATION, showBacNotification)
        editor.putInt(ACTIVE_THEME, activeTheme)

        editor.apply()

        val newPrefs = this.copy(
            profileInit = profileInit,
            sex = sex,
            weight = weight,
            weightMeasurement = weightMeasurement,
            startTimeMin = startTimeMin,
            endTimeMin = endTimeMin,
            use24HourTime = use24HourTime,
            dateInstalled = dateInstalled,
            drinksAddedCount = drinksAddedCount,
            dontShowRateDialog = dontShowRateDialog,
            dontShowCurrentBacNotification = dontShowCurrentBacNotification,
            showBacNotification = showBacNotification,
            activeTheme = activeTheme
        )
        Log.d(logTag, "Updated shared preferences to: $newPrefs")
        return newPrefs
    }
}

fun NightsOutActivity.getNightsOutSharedPreferences() =
    with(PreferenceManager.getDefaultSharedPreferences(this)) {
        NightsOutSharedPreferences(
            preferenceManager = this,
            profileInit = getBoolean(PROFILE_INIT, false),
            sex = all[PROFILE_SEX] as Boolean?,
            weight = getFloat(PROFILE_WEIGHT, 0F).toDouble(),
            weightMeasurement = WeightMeasurement.fromLowercaseString(
                this.getString(PROFILE_WEIGHT_MEASUREMENT, null) ?:
                (if(isCountryThatUsesLbs()) WeightMeasurement.LBS else WeightMeasurement.KG).displayName
            ),
            startTimeMin = getInt(START_TIME, getCurrentTimeInMinuets()),
            endTimeMin = getInt(END_TIME, getCurrentTimeInMinuets()),
            use24HourTime = getBoolean(USE_24_HOUR_TIME, isCountryThatUses12HourTime().not()),
            dateInstalled = getLong(DATE_INSTALLED, 0),
            drinksAddedCount = getInt(DRINKS_ADDED_COUNT, 0),
            dontShowRateDialog = getBoolean(DONT_SHOW_RATE_DIALOG, false),
            dontShowCurrentBacNotification = getBoolean(DONT_SHOW_BAC_NOTIFICATION, false),
            showBacNotification = getBoolean(SHOW_BAC_NOTIFICATION, true),
            activeTheme = getInt(ACTIVE_THEME, R.style.AppTheme)
        )
    }
