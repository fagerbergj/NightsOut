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
<<<<<<< HEAD
        val modifiedTime: Long = Calendar.getInstance().timeInMillis,
        val dontSuggest: Boolean = false
=======
        val modifiedTime: Long = Calendar.getInstance().timeInMillis
>>>>>>> c2164663bf4ca522472eb12d3d857c4dadc756c5
)
