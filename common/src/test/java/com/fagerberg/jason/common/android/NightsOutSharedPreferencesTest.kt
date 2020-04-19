package com.fagerberg.jason.common.android

import android.content.SharedPreferences
import android.util.Log
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
import com.fagerberg.jason.common.models.WeightMeasurement
import com.fagerberg.jason.common.utils.getCurrentTimeInMinuets
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class NightsOutSharedPreferencesTest {

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @Test
    fun `write returns correct copy`() {
        val mockEdit = mockk<SharedPreferences.Editor>(relaxed = true)
        val mockPrefManager = mockk<SharedPreferences>(relaxed = true) {
            every { edit() } returns mockEdit
        }

        val originalPrefs = NightsOutSharedPreferences(
            preferenceManager = mockPrefManager,
            profileInit = false,
            sex = null,
            weight = 0.0,
            weightMeasurement = null,
            startTimeMin = 0,
            endTimeMin = 0,
            use24HourTime = false,
            dateInstalled = 0,
            drinksAddedCount = 0,
            dontShowRateDialog = false,
            dontShowCurrentBacNotification = false,
            showBacNotification = false,
            activeTheme = 0
        )
        val newPrefs = NightsOutSharedPreferences(
            preferenceManager = mockPrefManager,
            profileInit = true,
            sex = true,
            weight = Math.random() * 200,
            weightMeasurement = WeightMeasurement.KG,
            startTimeMin = getCurrentTimeInMinuets(),
            endTimeMin = getCurrentTimeInMinuets(),
            use24HourTime = true,
            dateInstalled = (Math.random() * 100000).toLong(),
            drinksAddedCount = (Math.random() * 100).toInt(),
            dontShowRateDialog = true,
            dontShowCurrentBacNotification = true,
            showBacNotification = true,
            activeTheme = (Math.random() * 20000).toInt()
        )

        // verify correct new object returned
        assertThat(originalPrefs.update(
            profileInit = newPrefs.profileInit,
            sex = newPrefs.sex,
            weight = newPrefs.weight,
            weightMeasurement = newPrefs.weightMeasurement,
            startTimeMin = newPrefs.startTimeMin,
            endTimeMin = newPrefs.endTimeMin,
            use24HourTime = newPrefs.use24HourTime,
            dateInstalled = newPrefs.dateInstalled,
            drinksAddedCount = newPrefs.drinksAddedCount,
            dontShowRateDialog = newPrefs.dontShowRateDialog,
            dontShowCurrentBacNotification = newPrefs.dontShowCurrentBacNotification,
            showBacNotification = newPrefs.showBacNotification,
            activeTheme = newPrefs.activeTheme
        )).isEqualTo(newPrefs)

        // verify changes stored
        verify(exactly = 1) { mockPrefManager.edit() }

        verify(exactly = 1) { mockEdit.putBoolean(PROFILE_INIT, newPrefs.profileInit) }
        verify(exactly = 1) { mockEdit.putBoolean(PROFILE_SEX, newPrefs.sex!!) }
        verify(exactly = 1) { mockEdit.putFloat(PROFILE_WEIGHT, newPrefs.weight.toFloat()) }
        verify(exactly = 1) { mockEdit.putString(PROFILE_WEIGHT_MEASUREMENT, newPrefs.weightMeasurement!!.displayName) }
        verify(exactly = 1) { mockEdit.putInt(START_TIME, newPrefs.startTimeMin) }
        verify(exactly = 1) { mockEdit.putInt(END_TIME, newPrefs.endTimeMin) }
        verify(exactly = 1) { mockEdit.putLong(DATE_INSTALLED, newPrefs.dateInstalled) }
        verify(exactly = 1) { mockEdit.putInt(DRINKS_ADDED_COUNT, newPrefs.drinksAddedCount) }
        verify(exactly = 1) { mockEdit.putBoolean(DONT_SHOW_RATE_DIALOG, newPrefs.dontShowRateDialog) }
        verify(exactly = 1) { mockEdit.putBoolean(DONT_SHOW_BAC_NOTIFICATION, newPrefs.dontShowCurrentBacNotification) }
        verify(exactly = 1) { mockEdit.putBoolean(SHOW_BAC_NOTIFICATION, newPrefs.showBacNotification) }
        verify(exactly = 1) { mockEdit.putInt(ACTIVE_THEME, newPrefs.activeTheme) }

        verify(exactly = 1) { mockEdit.apply() }
    }
}
