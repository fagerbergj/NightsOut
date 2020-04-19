package com.fagerberg.jason.common.models

import java.util.Calendar
import java.util.UUID

data class Drink (
        val id: UUID,
        val name: String,
        val abv: Double,
        val amount: Double,
        val measurement: VolumeMeasurement,
        val favorited: Boolean,
        val recent: Boolean,
        val modifiedTime: Long = Calendar.getInstance().timeInMillis,
        val dontSuggest: Boolean = false
)
