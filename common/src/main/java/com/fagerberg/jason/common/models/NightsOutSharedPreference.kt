package com.fagerberg.jason.common.models

enum class NightsOutSharedPreference(val preferenceName: String) {
    PROFILE_INIT("profileInit"),
    DATE_INSTALLED("dateInstalled"),
    DRINKS_ADDED_COUNT("drinksAddedCount"),
    DONT_SHOW_RATE_DIALOG("dontShowRateDialog"),
    DONT_SHOW_BAC_NOTIFICATION("dontShowCurrentBacNotification"),
    SHOW_BAC_NOTIFICATION("showCurrentBacNotification"),
    ACTIVE_THEME("activeTheme"),
    PROFILE_SEX("profileSex"),
    PROFILE_WEIGHT("profileWeight"),
    PROFILE_WEIGHT_MEASUREMENT("profileWeightMeasurement"),
    USE_24_HOUR_TIME("homeUse24HourTime"),
    START_TIME("homeStartTimeMin"),
    END_TIME("homeEndTimeMin")
}
