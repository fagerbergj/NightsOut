package com.fagerberg.jason.common.models

import java.util.UUID

data class Drink (
        val id: UUID,
        var name: String,
        var abv: Double = 0.0,
        var amount: Double = 0.0,
        var measurement: VolumeMeasurement = VolumeMeasurement.OZ,
        var favorited: Boolean = false,
        var recent: Boolean = false,
        var modifiedTime: Long = 0
)
