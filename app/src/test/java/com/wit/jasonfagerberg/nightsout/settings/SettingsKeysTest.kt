package com.wit.jasonfagerberg.nightsout.settings

import com.wit.jasonfagerberg.nightsout.constants.Constants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SettingsKeysTest {

    @Test
    fun `datastore keys are built from the Constants preference names`() {
        assertThat(SettingsKeys.PROFILE_INIT.name).isEqualTo(Constants.PREFERENCE.PROFILE_INIT)
        assertThat(SettingsKeys.DATE_INSTALLED.name).isEqualTo(Constants.PREFERENCE.DATE_INSTALLED)
        assertThat(SettingsKeys.DRINKS_ADDED_COUNT.name).isEqualTo(Constants.PREFERENCE.DRINKS_ADDED_COUNT)
        assertThat(SettingsKeys.DONT_SHOW_RATE_DIALOG.name).isEqualTo(Constants.PREFERENCE.DONT_SHOW_RATE_DIALOG)
        assertThat(SettingsKeys.DONT_SHOW_BAC_NOTIFICATION.name).isEqualTo(Constants.PREFERENCE.DONT_SHOW_BAC_NOTIFICATION)
        assertThat(SettingsKeys.SHOW_BAC_NOTIFICATION.name).isEqualTo(Constants.PREFERENCE.SHOW_BAC_NOTIFICATION)
        assertThat(SettingsKeys.ACTIVE_THEME.name).isEqualTo(Constants.PREFERENCE.ACTIVE_THEME)
        assertThat(SettingsKeys.PROFILE_SEX.name).isEqualTo(Constants.PREFERENCE.PROFILE_SEX)
        assertThat(SettingsKeys.PROFILE_WEIGHT.name).isEqualTo(Constants.PREFERENCE.PROFILE_WEIGHT)
        assertThat(SettingsKeys.PROFILE_WEIGHT_MEASUREMENT.name).isEqualTo(Constants.PREFERENCE.PROFILE_WEIGHT_MEASUREMENT)
        assertThat(SettingsKeys.USE_24_HOUR_TIME.name).isEqualTo(Constants.PREFERENCE.USE_24_HOUR_TIME)
        assertThat(SettingsKeys.START_TIME.name).isEqualTo(Constants.PREFERENCE.START_TIME)
        assertThat(SettingsKeys.END_TIME.name).isEqualTo(Constants.PREFERENCE.END_TIME)
        assertThat(SettingsKeys.IS_BAC_NOTIFICATION_STARTED.name).isEqualTo(Constants.PREFERENCE.IS_BAC_NOTIFICATION_STARTED)
    }

    @Test
    fun `preference names are unchanged so existing users migrate from SharedPreferences`() {
        assertThat(Constants.PREFERENCE.PROFILE_INIT).isEqualTo("profileInit")
        assertThat(Constants.PREFERENCE.DATE_INSTALLED).isEqualTo("dateInstalled")
        assertThat(Constants.PREFERENCE.DRINKS_ADDED_COUNT).isEqualTo("drinksAddedCount")
        assertThat(Constants.PREFERENCE.DONT_SHOW_RATE_DIALOG).isEqualTo("dontShowRateDialog")
        assertThat(Constants.PREFERENCE.DONT_SHOW_BAC_NOTIFICATION).isEqualTo("dontShowCurrentBacNotification")
        assertThat(Constants.PREFERENCE.SHOW_BAC_NOTIFICATION).isEqualTo("showCurrentBacNotification")
        assertThat(Constants.PREFERENCE.ACTIVE_THEME).isEqualTo("activeTheme")
        assertThat(Constants.PREFERENCE.PROFILE_SEX).isEqualTo("profileSex")
        assertThat(Constants.PREFERENCE.PROFILE_WEIGHT).isEqualTo("profileWeight")
        assertThat(Constants.PREFERENCE.PROFILE_WEIGHT_MEASUREMENT).isEqualTo("profileWeightMeasurement")
        assertThat(Constants.PREFERENCE.USE_24_HOUR_TIME).isEqualTo("homeUse24HourTime")
        assertThat(Constants.PREFERENCE.START_TIME).isEqualTo("homeStartTimeMin")
        assertThat(Constants.PREFERENCE.END_TIME).isEqualTo("homeEndTimeMin")
        assertThat(Constants.PREFERENCE.IS_BAC_NOTIFICATION_STARTED).isEqualTo("isBacNotificationStarted")
    }
}
